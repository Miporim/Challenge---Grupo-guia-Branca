package br.com.fiap.aguiabranca.ia;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.auditoria.Acao;
import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteResponse;
import br.com.fiap.aguiabranca.ia.dto.ChatRequest;
import br.com.fiap.aguiabranca.ia.dto.ConversaResponse;
import br.com.fiap.aguiabranca.ia.dto.InsightsResponse;
import br.com.fiap.aguiabranca.ia.dto.PontuacaoIaResposta;
import br.com.fiap.aguiabranca.ideia.AnaliseIa;
import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.relatorio.RelatorioService;
import br.com.fiap.aguiabranca.relatorio.dto.PorEstrategiaResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse;
import br.com.fiap.aguiabranca.security.SecurityUtils;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Ponto único de acesso a IA (seção 7.4) — os controllers não conhecem
 * Gemini/Groq/Ollama, só {@link IaClient}. Timeout e retentativa ficam
 * no client (10s, 1 retentativa); aqui só o fallback: se o client
 * lançar {@link IaIndisponivelException}, ela sobe como está (502/503
 * tratados em {@link IaExceptionHandler}).
 */
@Service
@RequiredArgsConstructor
public class IaService {

    private final IdeiaService ideiaService;
    private final EstrategiaService estrategiaService;
    private final ConversaIaRepository conversaIaRepository;
    private final IaClient iaClient;
    private final PromptLoader promptLoader;
    private final AuditoriaPublisher auditoriaPublisher;
    private final MeterRegistry meterRegistry;
    private final RelatorioService relatorioService;
    private final InsightsCache insightsCache;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize("hasRole('GESTOR')")
    public AnaliseIa analisarIdeia(String ideiaId) {
        Ideia ideia = ideiaService.buscarPorId(ideiaId);
        Estrategia estrategia = estrategiaService.buscarPorId(ideia.getEstrategiaId());

        long inicio = System.nanoTime();
        String resultadoMetrica = "erro";
        try {
            String prompt = promptLoader.carregar("pontuacao-ideia.txt", Map.of(
                    "estrategiaTitulo", estrategia.getTitulo(),
                    "estrategiaCategoria", estrategia.getCategoria().name(),
                    "estrategiaDescricao", estrategia.getDescricao(),
                    "ideiaTitulo", ideia.getTitulo(),
                    "ideiaDescricao", ideia.getDescricao(),
                    "impacto", ideia.getImpacto().name(),
                    "esforco", ideia.getEsforco().name()));

            String respostaTexto = iaClient.gerarTexto(prompt);
            PontuacaoIaResposta resposta = parsearEValidarPontuacao(respostaTexto);

            AnaliseIa analise = AnaliseIa.builder()
                    .score(resposta.score())
                    .aderenciaEstrategia(resposta.aderenciaEstrategia())
                    .justificativa(resposta.justificativa())
                    .riscos(resposta.riscos())
                    .modelo(iaClient.nomeModelo())
                    .geradoEm(Instant.now())
                    .build();

            ideiaService.registrarAnaliseIa(ideiaId, analise);
            resultadoMetrica = "sucesso";
            return analise;
        } finally {
            registrarMetricaEAuditoria("pontuacao", resultadoMetrica, inicio, "ideia", ideiaId);
        }
    }

    @PreAuthorize("hasRole('GESTOR')")
    public AnaliseLoteResponse analisarLote(String estrategiaId) {
        List<Ideia> submetidas = ideiaService.listarSubmetidasPorEstrategia(estrategiaId);
        int sucesso = 0;
        int falha = 0;
        for (Ideia ideia : submetidas) {
            try {
                // DECISION: chamada direta (self-invocation) — o
                // @PreAuthorize de analisarIdeia não é reavaliado pelo
                // proxy aqui, mas o método já está protegido por
                // hasRole('GESTOR') neste ponto de entrada, então não
                // há brecha de autorização.
                analisarIdeia(ideia.getId());
                sucesso++;
            } catch (RuntimeException ex) {
                falha++;
            }
        }
        return new AnaliseLoteResponse(sucesso, falha);
    }

    @PreAuthorize("hasRole('GESTOR') and (#request.conversaId() == null "
            + "or @conversaSecurity.ehDono(#request.conversaId(), authentication))")
    public ConversaResponse chat(ChatRequest request) {
        String gestorId = SecurityUtils.getUsuarioId();
        ConversaIa conversa = request.conversaId() != null
                ? conversaIaRepository.findById(request.conversaId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + request.conversaId()))
                : novaConversa(gestorId, request.mensagem());

        Estrategia vigente = estrategiaService.vigenteAtual();
        List<Ideia> submetidas = ideiaService.listarSubmetidasPorEstrategia(vigente.getId());
        String ideiasTexto = submetidas.isEmpty()
                ? "(nenhuma ideia submetida nesta estratégia)"
                : submetidas.stream().map(i -> "- " + i.getTitulo() + ": " + i.getDescricao())
                        .collect(Collectors.joining("\n"));

        String historicoTexto = conversa.getMensagens().isEmpty()
                ? "(início da conversa)"
                : conversa.getMensagens().stream().map(m -> m.getPapel() + ": " + m.getTexto())
                        .collect(Collectors.joining("\n"));

        long inicio = System.nanoTime();
        String resultadoMetrica = "erro";
        try {
            String prompt = promptLoader.carregar("chat-assistente.txt", Map.of(
                    "estrategiaTitulo", vigente.getTitulo(),
                    "estrategiaCategoria", vigente.getCategoria().name(),
                    "estrategiaDescricao", vigente.getDescricao(),
                    "ideiasSubmetidas", ideiasTexto,
                    "historicoConversa", historicoTexto,
                    "mensagemUsuario", request.mensagem()));

            String respostaTexto = iaClient.gerarTexto(prompt);

            List<MensagemIa> mensagens = new ArrayList<>(conversa.getMensagens());
            mensagens.add(MensagemIa.builder().papel("user").texto(request.mensagem()).em(Instant.now()).build());
            mensagens.add(MensagemIa.builder().papel("assistant").texto(respostaTexto).em(Instant.now()).build());
            conversa.setMensagens(mensagens);
            conversa.setAtualizadoEm(Instant.now());

            ConversaIa salva = conversaIaRepository.save(conversa);
            resultadoMetrica = "sucesso";
            return ConversaResponse.de(salva);
        } finally {
            registrarMetricaEAuditoria("chat", resultadoMetrica, inicio, "ia_conversa", conversa.getId());
        }
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Page<ConversaResponse> listarConversas(Pageable pageable) {
        return conversaIaRepository.findByGestorId(SecurityUtils.getUsuarioId(), pageable)
                .map(ConversaResponse::de);
    }

    @PreAuthorize("@conversaSecurity.ehDono(#id, authentication)")
    public ConversaResponse buscarConversa(String id) {
        ConversaIa conversa = conversaIaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + id));
        return ConversaResponse.de(conversa);
    }

    @PreAuthorize("hasRole('LIDER')")
    public InsightsResponse insights() {
        String cacheado = insightsCache.obter();
        if (cacheado != null) {
            try {
                return objectMapper.readValue(cacheado, InsightsResponse.class);
            } catch (Exception ex) {
                // Cache corrompido (não deveria acontecer) — recalcula.
            }
        }

        ResumoResponse resumo = relatorioService.resumo();
        List<PorEstrategiaResponse> porEstrategia = relatorioService.porEstrategia();

        long inicio = System.nanoTime();
        String resultadoMetrica = "erro";
        try {
            String dadosJson = escreverJson(Map.of("resumo", resumo, "porEstrategia", porEstrategia));
            String prompt = promptLoader.carregar("insights.txt", Map.of("dadosRelatorio", dadosJson));

            String respostaTexto = iaClient.gerarTexto(prompt);
            InsightsResponse insights = parsearEValidarInsights(respostaTexto);

            insightsCache.guardar(escreverJson(insights));
            resultadoMetrica = "sucesso";
            return insights;
        } finally {
            registrarMetricaEAuditoria("insights", resultadoMetrica, inicio, "relatorio", null);
        }
    }

    private ConversaIa novaConversa(String gestorId, String primeiraMensagem) {
        Instant agora = Instant.now();
        String titulo = primeiraMensagem.length() > 60 ? primeiraMensagem.substring(0, 60) + "…" : primeiraMensagem;
        return ConversaIa.builder()
                .gestorId(gestorId)
                .titulo(titulo)
                .mensagens(new ArrayList<>())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
    }

    private PontuacaoIaResposta parsearEValidarPontuacao(String textoResposta) {
        PontuacaoIaResposta resposta;
        try {
            resposta = objectMapper.readValue(extrairJson(textoResposta), PontuacaoIaResposta.class);
        } catch (Exception ex) {
            throw new IaRespostaInvalidaException("IA não retornou um JSON válido para a pontuação", ex);
        }
        boolean valido = resposta.score() != null && resposta.score() >= 0 && resposta.score() <= 100
                && resposta.aderenciaEstrategia() != null && resposta.aderenciaEstrategia() >= 0 && resposta.aderenciaEstrategia() <= 100
                && resposta.justificativa() != null && !resposta.justificativa().isBlank() && resposta.justificativa().length() <= 400
                && resposta.riscos() != null && !resposta.riscos().isEmpty();
        if (!valido) {
            throw new IaRespostaInvalidaException("Resposta da IA não passou na validação do schema de pontuação");
        }
        return resposta;
    }

    private InsightsResponse parsearEValidarInsights(String textoResposta) {
        InsightsResponse resposta;
        try {
            resposta = objectMapper.readValue(extrairJson(textoResposta), InsightsResponse.class);
        } catch (Exception ex) {
            throw new IaRespostaInvalidaException("IA não retornou um JSON válido para insights", ex);
        }
        boolean valido = resposta.leituraGeral() != null && !resposta.leituraGeral().isBlank()
                && resposta.pontosAtencao() != null && !resposta.pontosAtencao().isEmpty()
                && resposta.recomendacoes() != null && !resposta.recomendacoes().isEmpty();
        if (!valido) {
            throw new IaRespostaInvalidaException("Resposta da IA não passou na validação do schema de insights");
        }
        return resposta;
    }

    /** Alguns provedores envolvem o JSON em ```json ... ``` mesmo quando instruídos a não fazer isso. */
    private String extrairJson(String texto) {
        int inicio = texto.indexOf('{');
        int fim = texto.lastIndexOf('}');
        if (inicio < 0 || fim < inicio) {
            return texto;
        }
        return texto.substring(inicio, fim + 1);
    }

    private String escreverJson(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao serializar dados para o prompt de IA", ex);
        }
    }

    private void registrarMetricaEAuditoria(String funcionalidade, String resultado, long inicioNanos, String recurso, String recursoId) {
        meterRegistry.counter("ia_chamadas_total", "funcionalidade", funcionalidade, "resultado", resultado).increment();
        meterRegistry.timer("ia_latencia").record(Duration.ofNanos(System.nanoTime() - inicioNanos));
        auditoriaPublisher.publicar(Acao.IA, recurso, recursoId, null,
                AuditoriaPublisher.mapa("funcionalidade", funcionalidade, "resultado", resultado));
    }
}
