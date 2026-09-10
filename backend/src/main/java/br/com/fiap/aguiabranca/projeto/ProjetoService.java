package br.com.fiap.aguiabranca.projeto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.projeto.dto.AtualizarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.CriarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProgressoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ResultadoRequest;
import br.com.fiap.aguiabranca.security.SecurityUtils;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import br.com.fiap.aguiabranca.shared.RequisicaoInvalidaException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final br.com.fiap.aguiabranca.estrategia.EstrategiaService estrategiaService;
    private final IdeiaService ideiaService;
    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasRole('GESTOR')")
    public Projeto criar(CriarProjetoRequest request) {
        validarPrazos(request.prazoInicio(), request.prazoFim());
        // Garante que a estratégia existe (404 se não) — seção 3 não exige
        // que esteja VIGENTE para receber um projeto, diferente da ideia.
        estrategiaService.buscarPorId(request.estrategiaId());

        if (request.ideiaOrigemId() != null) {
            Ideia ideia = ideiaService.buscarPorId(request.ideiaOrigemId());
            if (ideia.getProjetoId() != null) {
                throw new ConflitoException("Ideia já vinculada a outro projeto");
            }
        }

        Projeto projeto = Projeto.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .estrategiaId(request.estrategiaId())
                .ideiaOrigemId(request.ideiaOrigemId())
                .gestorId(SecurityUtils.getUsuarioId())
                .etapa(request.etapa() != null ? request.etapa() : EtapaProjeto.PLANEJAMENTO)
                .status(request.status() != null ? request.status() : StatusProjeto.NO_PRAZO)
                .investimento(request.investimento())
                .prazoInicio(request.prazoInicio())
                .prazoFim(request.prazoFim())
                .percentualConcluido(0)
                .resultados(new ArrayList<>())
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();

        Projeto salvo = projetoRepository.save(projeto);

        if (request.ideiaOrigemId() != null) {
            ideiaService.vincularProjeto(request.ideiaOrigemId(), salvo.getId());
        }

        return salvo;
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Projeto atualizar(String id, AtualizarProjetoRequest request) {
        validarPrazos(request.prazoInicio(), request.prazoFim());
        estrategiaService.buscarPorId(request.estrategiaId());

        Projeto projeto = buscarPorId(id);
        projeto.setTitulo(request.titulo());
        projeto.setDescricao(request.descricao());
        projeto.setEstrategiaId(request.estrategiaId());
        projeto.setInvestimento(request.investimento());
        projeto.setPrazoInicio(request.prazoInicio());
        projeto.setPrazoFim(request.prazoFim());
        projeto.setAtualizadoEm(Instant.now());

        return projetoRepository.save(projeto);
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Projeto atualizarProgresso(String id, ProgressoRequest request) {
        Projeto projeto = buscarPorId(id);

        if (request.status() != null) {
            projeto.setStatus(request.status());
        }
        if (request.etapa() != null) {
            projeto.setEtapa(request.etapa());
            if (request.etapa() == EtapaProjeto.CONCLUIDO) {
                projeto.setConcluidoEm(Instant.now());
                projeto.setPercentualConcluido(100);
            } else if (request.percentualConcluido() != null) {
                projeto.setPercentualConcluido(request.percentualConcluido());
            }
        } else if (request.percentualConcluido() != null) {
            projeto.setPercentualConcluido(request.percentualConcluido());
        }
        projeto.setAtualizadoEm(Instant.now());

        return projetoRepository.save(projeto);
    }

    @PreAuthorize("hasRole('GESTOR')")
    public Projeto registrarResultado(String id, ResultadoRequest request) {
        Projeto projeto = buscarPorId(id);

        Resultado resultado = Resultado.builder()
                .data(request.data())
                .receita(request.receita())
                .economia(request.economia())
                .ganhoProdutividadePct(request.ganhoProdutividadePct())
                .observacao(request.observacao())
                .registradoPor(SecurityUtils.getUsuarioId())
                .build();

        List<Resultado> resultados = new ArrayList<>(projeto.getResultados());
        resultados.add(resultado);
        projeto.setResultados(resultados);
        projeto.setAtualizadoEm(Instant.now());

        return projetoRepository.save(projeto);
    }

    @PreAuthorize("hasRole('GESTOR')")
    public void excluir(String id) {
        Projeto projeto = buscarPorId(id);
        projetoRepository.delete(projeto);
    }

    public Projeto buscarPorId(String id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + id));
    }

    public Page<Projeto> listar(EtapaProjeto etapa, StatusProjeto status, String estrategiaId, Pageable pageable) {
        Query query = new Query();
        List<Criteria> filtros = new ArrayList<>();

        if (etapa != null) {
            filtros.add(Criteria.where("etapa").is(etapa));
        }
        if (status != null) {
            filtros.add(Criteria.where("status").is(status));
        }
        if (estrategiaId != null && !estrategiaId.isBlank()) {
            filtros.add(Criteria.where("estrategiaId").is(estrategiaId));
        }
        if (!filtros.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filtros.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Projeto.class);
        List<Projeto> pagina = mongoTemplate.find(query.with(pageable), Projeto.class);
        return PageableExecutionUtils.getPage(pagina, pageable, () -> total);
    }

    private void validarPrazos(Instant prazoInicio, Instant prazoFim) {
        if (!prazoFim.isAfter(prazoInicio)) {
            throw new RequisicaoInvalidaException("prazoFim deve ser posterior a prazoInicio");
        }
    }
}
