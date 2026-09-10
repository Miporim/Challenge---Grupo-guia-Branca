package br.com.fiap.aguiabranca.ideia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.dto.IdeiaRequest;
import br.com.fiap.aguiabranca.ideia.dto.PriorizacaoRequest;
import br.com.fiap.aguiabranca.ideia.dto.StatusIdeiaRequest;
import br.com.fiap.aguiabranca.security.SecurityUtils;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import br.com.fiap.aguiabranca.shared.RequisicaoInvalidaException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdeiaService {

    private static final Set<StatusIdeia> STATUS_PERMITIDOS_NA_CRIACAO = Set.of(StatusIdeia.RASCUNHO, StatusIdeia.SUBMETIDA);
    private static final Set<StatusIdeia> STATUS_EDITAVEIS = Set.of(StatusIdeia.RASCUNHO, StatusIdeia.SUBMETIDA);
    private static final Set<StatusIdeia> STATUS_EM_REVISAO = Set.of(StatusIdeia.SUBMETIDA, StatusIdeia.EM_ANALISE);
    private static final Set<StatusIdeia> STATUS_DECISAO_PERMITIDA = Set.of(StatusIdeia.APROVADA, StatusIdeia.REPROVADA);

    private final IdeiaRepository ideiaRepository;
    private final EstrategiaService estrategiaService;
    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasRole('OPERADOR')")
    public Ideia criar(IdeiaRequest request) {
        StatusIdeia status = validarStatusDeEntrada(request.status());
        Estrategia estrategia = validarEstrategiaVigente(request.estrategiaId());

        Ideia ideia = Ideia.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .autorId(SecurityUtils.getUsuarioId())
                .estrategiaId(estrategia.getId())
                .status(status)
                .impacto(request.impacto())
                .esforco(request.esforco())
                .priorizacoes(new ArrayList<>())
                .notaMedia(BigDecimal.ZERO)
                .totalVotos(0)
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();

        return ideiaRepository.save(ideia);
    }

    @PreAuthorize("@ideiaSecurity.ehAutor(#id, authentication)")
    public Ideia atualizar(String id, IdeiaRequest request) {
        Ideia ideia = buscarPorId(id);

        if (!STATUS_EDITAVEIS.contains(ideia.getStatus())) {
            throw new ConflitoException("Ideia só pode ser editada em RASCUNHO ou SUBMETIDA");
        }

        StatusIdeia novoStatus = validarStatusDeEntrada(request.status() != null ? request.status() : ideia.getStatus());
        Estrategia estrategia = validarEstrategiaVigente(request.estrategiaId());

        ideia.setTitulo(request.titulo());
        ideia.setDescricao(request.descricao());
        ideia.setEstrategiaId(estrategia.getId());
        ideia.setImpacto(request.impacto());
        ideia.setEsforco(request.esforco());
        ideia.setStatus(novoStatus);
        ideia.setAtualizadoEm(Instant.now());

        return ideiaRepository.save(ideia);
    }

    @PreAuthorize("hasRole('GESTOR') or @ideiaSecurity.ehAutor(#id, authentication)")
    public void excluir(String id) {
        Ideia ideia = buscarPorId(id);
        if (ideia.getStatus() == StatusIdeia.APROVADA) {
            throw new ConflitoException("Ideia aprovada não pode ser excluída");
        }
        ideiaRepository.delete(ideia);
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Ideia priorizar(String id, PriorizacaoRequest request) {
        Ideia ideia = buscarPorId(id);

        if (!STATUS_EM_REVISAO.contains(ideia.getStatus())) {
            // DECISION: só é possível priorizar ideias em SUBMETIDA ou
            // EM_ANALISE — não explícito na seção 4, mas coerente com a
            // regra de que aprovar/reprovar só vale nesses estados.
            throw new ConflitoException("Ideia não está em um estado priorizável");
        }

        String gestorId = SecurityUtils.getUsuarioId();
        List<Priorizacao> priorizacoes = new ArrayList<>(ideia.getPriorizacoes());
        priorizacoes.removeIf(p -> p.getGestorId().equals(gestorId));
        priorizacoes.add(Priorizacao.builder()
                .gestorId(gestorId)
                .nota(request.nota())
                .comentario(request.comentario())
                .em(Instant.now())
                .build());

        ideia.setPriorizacoes(priorizacoes);
        ideia.setTotalVotos(priorizacoes.size());
        ideia.setNotaMedia(calcularNotaMedia(priorizacoes));

        if (ideia.getStatus() == StatusIdeia.SUBMETIDA) {
            ideia.setStatus(StatusIdeia.EM_ANALISE);
        }
        ideia.setAtualizadoEm(Instant.now());

        return ideiaRepository.save(ideia);
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Ideia decidir(String id, StatusIdeiaRequest request) {
        if (!STATUS_DECISAO_PERMITIDA.contains(request.status())) {
            throw new RequisicaoInvalidaException("Status de decisão deve ser APROVADA ou REPROVADA");
        }

        Ideia ideia = buscarPorId(id);
        if (!STATUS_EM_REVISAO.contains(ideia.getStatus())) {
            throw new ConflitoException("Ideia só pode ser aprovada/reprovada a partir de SUBMETIDA ou EM_ANALISE");
        }

        // TODO (Etapa F): registrar request.justificativa() no evento de
        // auditoria APROVAR/REPROVAR — não persistida na ideia (não faz
        // parte do modelo de dados da seção 3).
        ideia.setStatus(request.status());
        ideia.setAtualizadoEm(Instant.now());

        return ideiaRepository.save(ideia);
    }

    @PreAuthorize("hasRole('OPERADOR')")
    public Page<Ideia> listarMinhas(Pageable pageable) {
        return ideiaRepository.findByAutorId(SecurityUtils.getUsuarioId(), pageable);
    }

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER')")
    public Page<Ideia> listar(StatusIdeia status, String estrategiaId, Pageable pageable) {
        Query query = new Query();
        List<Criteria> filtros = new ArrayList<>();

        if (status != null) {
            filtros.add(Criteria.where("status").is(status));
        }
        if (estrategiaId != null && !estrategiaId.isBlank()) {
            filtros.add(Criteria.where("estrategiaId").is(estrategiaId));
        }
        if (!filtros.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filtros.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Ideia.class);
        List<Ideia> pagina = mongoTemplate.find(query.with(pageable), Ideia.class);
        return PageableExecutionUtils.getPage(pagina, pageable, () -> total);
    }

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER') or @ideiaSecurity.ehAutor(#id, authentication)")
    public Ideia buscarParaVisualizacao(String id) {
        return buscarPorId(id);
    }

    public Ideia buscarPorId(String id) {
        return ideiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia não encontrada: " + id));
    }

    private StatusIdeia validarStatusDeEntrada(StatusIdeia status) {
        if (status == null) {
            return StatusIdeia.SUBMETIDA;
        }
        if (!STATUS_PERMITIDOS_NA_CRIACAO.contains(status)) {
            throw new RequisicaoInvalidaException("Status inicial deve ser RASCUNHO ou SUBMETIDA");
        }
        return status;
    }

    private Estrategia validarEstrategiaVigente(String estrategiaId) {
        Estrategia estrategia = estrategiaService.buscarPorId(estrategiaId);
        if (estrategia.getStatus() != EstrategiaStatus.VIGENTE) {
            throw new RequisicaoInvalidaException("A estratégia referenciada não está vigente");
        }
        return estrategia;
    }

    private BigDecimal calcularNotaMedia(List<Priorizacao> priorizacoes) {
        if (priorizacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int soma = priorizacoes.stream().mapToInt(Priorizacao::getNota).sum();
        return BigDecimal.valueOf(soma)
                .divide(BigDecimal.valueOf(priorizacoes.size()), 2, RoundingMode.HALF_UP);
    }
}
