package br.com.fiap.aguiabranca.estrategia;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaRequest;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.security.SecurityUtils;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstrategiaService {

    private final EstrategiaRepository estrategiaRepository;
    private final MongoTemplate mongoTemplate;
    private final IdeiaRepository ideiaRepository;

    @PreAuthorize("hasRole('LIDER')")
    public Estrategia criar(EstrategiaRequest request) {
        EstrategiaStatus status = request.status() != null ? request.status() : EstrategiaStatus.VIGENTE;

        Estrategia estrategia = Estrategia.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .campanha(request.campanha())
                .vigenciaInicio(request.vigenciaInicio())
                .vigenciaFim(request.vigenciaFim())
                .status(status)
                .criadoPor(SecurityUtils.getUsuarioId())
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();

        Estrategia salva = estrategiaRepository.save(estrategia);

        if (status == EstrategiaStatus.VIGENTE) {
            encerrarOutrasVigentesDaCampanha(request.campanha(), salva.getId());
        }

        return salva;
    }

    @PreAuthorize("hasRole('LIDER')")
    public Estrategia atualizar(String id, EstrategiaRequest request) {
        Estrategia estrategia = buscarPorId(id);

        estrategia.setTitulo(request.titulo());
        estrategia.setDescricao(request.descricao());
        estrategia.setCategoria(request.categoria());
        estrategia.setCampanha(request.campanha());
        estrategia.setVigenciaInicio(request.vigenciaInicio());
        estrategia.setVigenciaFim(request.vigenciaFim());
        // DECISION: se o PUT também setar status VIGENTE, aplicamos a mesma
        // regra da seção 2.3 (encerra a anterior da campanha) — a
        // especificação só descreve isso para o POST, mas deixar o PUT
        // criar uma segunda vigente na mesma campanha contradiria a
        // decisão 3. Se status vier nulo, mantém o atual.
        EstrategiaStatus novoStatus = request.status() != null ? request.status() : estrategia.getStatus();
        estrategia.setStatus(novoStatus);
        estrategia.setAtualizadoEm(Instant.now());

        Estrategia salva = estrategiaRepository.save(estrategia);

        if (novoStatus == EstrategiaStatus.VIGENTE) {
            encerrarOutrasVigentesDaCampanha(request.campanha(), salva.getId());
        }

        return salva;
    }

    @PreAuthorize("hasRole('LIDER')")
    public Estrategia encerrar(String id) {
        Estrategia estrategia = buscarPorId(id);
        estrategia.setStatus(EstrategiaStatus.ENCERRADA);
        estrategia.setAtualizadoEm(Instant.now());
        return estrategiaRepository.save(estrategia);
    }

    @PreAuthorize("hasRole('LIDER')")
    public void excluir(String id) {
        Estrategia estrategia = buscarPorId(id);
        if (ideiaRepository.existsByEstrategiaId(id)) {
            throw new ConflitoException("Há ideias vinculadas a esta estratégia");
        }
        // TODO (Etapa D): devolver 409 também se houver projetos vinculados
        // — ProjetoRepository ainda não existe.
        estrategiaRepository.delete(estrategia);
    }

    public Estrategia buscarPorId(String id) {
        return estrategiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estratégia não encontrada: " + id));
    }

    /**
     * DECISION: "a estratégia vigente" (sem filtro de campanha) — pode
     * haver uma vigente por campanha (decisão 3), então isto devolve a
     * mais recente por {@code vigenciaInicio}, coerente com o índice
     * dedicado a esse campo. Não especificado explicitamente qual vigente
     * este endpoint deveria devolver quando há mais de uma campanha ativa.
     */
    public Estrategia vigenteAtual() {
        return estrategiaRepository.findFirstByStatusOrderByVigenciaInicioDesc(EstrategiaStatus.VIGENTE)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma estratégia vigente"));
    }

    public Page<Estrategia> listar(EstrategiaStatus status, Categoria categoria, String campanha, Pageable pageable) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        List<Criteria> filtros = new java.util.ArrayList<>();

        if (status != null) {
            filtros.add(Criteria.where("status").is(status));
        }
        if (categoria != null) {
            filtros.add(Criteria.where("categoria").is(categoria));
        }
        if (campanha != null && !campanha.isBlank()) {
            filtros.add(Criteria.where("campanha").is(campanha));
        }
        if (!filtros.isEmpty()) {
            criteria.andOperator(filtros.toArray(new Criteria[0]));
            query.addCriteria(criteria);
        }

        long total = mongoTemplate.count(query, Estrategia.class);
        List<Estrategia> pagina = mongoTemplate.find(query.with(pageable), Estrategia.class);

        return PageableExecutionUtils.getPage(pagina, pageable, () -> total);
    }

    private void encerrarOutrasVigentesDaCampanha(String campanha, String idEstrategiaAtual) {
        List<Estrategia> vigentes = estrategiaRepository.findByCampanhaAndStatus(campanha, EstrategiaStatus.VIGENTE);
        for (Estrategia vigente : vigentes) {
            if (vigente.getId().equals(idEstrategiaAtual)) {
                continue;
            }
            vigente.setStatus(EstrategiaStatus.ENCERRADA);
            vigente.setAtualizadoEm(Instant.now());
            estrategiaRepository.save(vigente);
        }
    }
}
