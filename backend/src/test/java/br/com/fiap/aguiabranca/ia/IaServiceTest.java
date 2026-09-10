package br.com.fiap.aguiabranca.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.Categoria;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteResponse;
import br.com.fiap.aguiabranca.ia.dto.ChatRequest;
import br.com.fiap.aguiabranca.ia.dto.ConversaResponse;
import br.com.fiap.aguiabranca.ia.dto.InsightsResponse;
import br.com.fiap.aguiabranca.ideia.AnaliseIa;
import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.ideia.Nivel;
import br.com.fiap.aguiabranca.relatorio.RelatorioService;
import br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Regras de negócio puras (sem Spring context) — a matriz de acesso tem
 * sua própria prova em {@link IaServiceMethodSecurityTest}. Usa o
 * {@link PromptLoader} real (lê os arquivos de
 * {@code src/main/resources/prompts/}) e mocka {@link IaClient} — nunca
 * chama uma IA de verdade.
 */
class IaServiceTest {

    private final IdeiaService ideiaService = mock(IdeiaService.class);
    private final EstrategiaService estrategiaService = mock(EstrategiaService.class);
    private final ConversaIaRepository conversaIaRepository = mock(ConversaIaRepository.class);
    private final IaClient iaClient = mock(IaClient.class);
    private final PromptLoader promptLoader = new PromptLoader();
    private final AuditoriaPublisher auditoriaPublisher = mock(AuditoriaPublisher.class);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final RelatorioService relatorioService = mock(RelatorioService.class);
    private final InsightsCache insightsCache = new InsightsCache();

    private final IaService iaService = new IaService(ideiaService, estrategiaService, conversaIaRepository,
            iaClient, promptLoader, auditoriaPublisher, meterRegistry, relatorioService, insightsCache);

    private final Estrategia estrategia = Estrategia.builder()
            .id("estrategia-1").titulo("Eficiência").categoria(Categoria.EFICIENCIA).descricao("Reduzir custos").build();

    private final Ideia ideia = Ideia.builder()
            .id("ideia-1").titulo("Rota otimizada").descricao("Otimizar rotas de entrega")
            .estrategiaId("estrategia-1").impacto(Nivel.ALTO).esforco(Nivel.MEDIO).build();

    @BeforeEach
    void autenticarComoGestor() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor-1", null, List.of()));
        when(ideiaService.buscarPorId("ideia-1")).thenReturn(ideia);
        when(estrategiaService.buscarPorId("estrategia-1")).thenReturn(estrategia);
        when(iaClient.nomeModelo()).thenReturn("gemini-2.5-flash-lite");
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analisarIdeiaComRespostaValidaPersisteAnalise() {
        when(iaClient.gerarTexto(anyString())).thenReturn(
                "{\"score\": 85, \"aderenciaEstrategia\": 70, \"justificativa\": \"Boa ideia\", \"riscos\": [\"Custo alto\"]}");
        when(ideiaService.registrarAnaliseIa(anyString(), any(AnaliseIa.class))).thenReturn(ideia);

        AnaliseIa resultado = iaService.analisarIdeia("ideia-1");

        assertEquals(85, resultado.getScore());
        assertEquals(70, resultado.getAderenciaEstrategia());
        assertEquals("gemini-2.5-flash-lite", resultado.getModelo());
        assertEquals(1.0, meterRegistry.counter("ia_chamadas_total", "funcionalidade", "pontuacao", "resultado", "sucesso").count());
    }

    @Test
    void analisarIdeiaComJsonMalformadoLancaRespostaInvalida() {
        when(iaClient.gerarTexto(anyString())).thenReturn("isto não é json");

        assertThrows(IaRespostaInvalidaException.class, () -> iaService.analisarIdeia("ideia-1"));
        assertEquals(1.0, meterRegistry.counter("ia_chamadas_total", "funcionalidade", "pontuacao", "resultado", "erro").count());
    }

    @Test
    void analisarIdeiaComScoreForaDoIntervaloLancaRespostaInvalida() {
        when(iaClient.gerarTexto(anyString())).thenReturn(
                "{\"score\": 150, \"aderenciaEstrategia\": 70, \"justificativa\": \"x\", \"riscos\": [\"a\"]}");

        assertThrows(IaRespostaInvalidaException.class, () -> iaService.analisarIdeia("ideia-1"));
    }

    @Test
    void analisarIdeiaComJustificativaMuitoLongaLancaRespostaInvalida() {
        String justificativaLonga = "x".repeat(401);
        when(iaClient.gerarTexto(anyString())).thenReturn(
                "{\"score\": 50, \"aderenciaEstrategia\": 50, \"justificativa\": \"" + justificativaLonga + "\", \"riscos\": [\"a\"]}");

        assertThrows(IaRespostaInvalidaException.class, () -> iaService.analisarIdeia("ideia-1"));
    }

    @Test
    void analisarIdeiaComProvedorIndisponivelNaoPersisteNada() {
        when(iaClient.gerarTexto(anyString())).thenThrow(new IaIndisponivelException("sem key"));

        assertThrows(IaIndisponivelException.class, () -> iaService.analisarIdeia("ideia-1"));
        org.mockito.Mockito.verify(ideiaService, org.mockito.Mockito.times(0)).registrarAnaliseIa(any(), any());
    }

    @Test
    void analisarLoteContaSucessosEFalhas() {
        Ideia ideia2 = Ideia.builder().id("ideia-2").titulo("T2").descricao("D2")
                .estrategiaId("estrategia-1").impacto(Nivel.BAIXO).esforco(Nivel.BAIXO).build();
        when(ideiaService.listarSubmetidasPorEstrategia("estrategia-1")).thenReturn(List.of(ideia, ideia2));
        when(ideiaService.buscarPorId("ideia-2")).thenReturn(ideia2);
        when(ideiaService.registrarAnaliseIa(anyString(), any(AnaliseIa.class))).thenReturn(ideia);
        when(iaClient.gerarTexto(anyString()))
                .thenReturn("{\"score\": 80, \"aderenciaEstrategia\": 60, \"justificativa\": \"ok\", \"riscos\": [\"a\"]}")
                .thenReturn("json invalido");

        AnaliseLoteResponse resultado = iaService.analisarLote("estrategia-1");

        assertEquals(1, resultado.totalAnalisadas());
        assertEquals(1, resultado.totalFalhas());
    }

    @Test
    void chatComConversaNovaPersisteAsDuasMensagens() {
        when(estrategiaService.vigenteAtual()).thenReturn(estrategia);
        when(ideiaService.listarSubmetidasPorEstrategia("estrategia-1")).thenReturn(List.of());
        when(iaClient.gerarTexto(anyString())).thenReturn("Aqui está minha resposta.");
        when(conversaIaRepository.save(any(ConversaIa.class))).thenAnswer(inv -> {
            ConversaIa c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId("conversa-1");
            }
            return c;
        });

        ConversaResponse resposta = iaService.chat(new ChatRequest(null, "Quais ideias temos?"));

        assertEquals(2, resposta.mensagens().size());
        assertEquals("user", resposta.mensagens().get(0).papel());
        assertEquals("assistant", resposta.mensagens().get(1).papel());
    }

    @Test
    void buscarConversaInexistenteLancaNaoEncontrado() {
        when(conversaIaRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException.class,
                () -> iaService.buscarConversa("inexistente"));
    }

    @Test
    void insightsComRespostaValidaCacheiaResultado() {
        when(relatorioService.resumo()).thenReturn(new ResumoResponse(
                0, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.util.Map.of(), java.util.Map.of()));
        when(relatorioService.porEstrategia()).thenReturn(List.of());
        when(iaClient.gerarTexto(anyString())).thenReturn(
                "{\"leituraGeral\": \"Tudo bem\", \"pontosAtencao\": [\"a\"], \"recomendacoes\": [\"b\"]}");

        InsightsResponse primeira = iaService.insights();
        InsightsResponse segunda = iaService.insights();

        assertEquals("Tudo bem", primeira.leituraGeral());
        assertEquals(primeira, segunda);
        // Só uma chamada real à IA — a segunda veio do cache.
        org.mockito.Mockito.verify(iaClient, org.mockito.Mockito.times(1)).gerarTexto(anyString());
    }
}
