package br.com.fiap.aguiabranca.auditoria;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasRole('LIDER')")
    public Page<AuditoriaEvento> listar(String ator, Acao acao, String recurso, Instant de, Instant ate, Pageable pageable) {
        Query query = new Query();
        List<Criteria> filtros = new ArrayList<>();

        if (ator != null && !ator.isBlank()) {
            filtros.add(Criteria.where("ator.userId").is(ator));
        }
        if (acao != null) {
            filtros.add(Criteria.where("acao").is(acao));
        }
        if (recurso != null && !recurso.isBlank()) {
            filtros.add(Criteria.where("recurso").is(recurso));
        }
        if (de != null) {
            filtros.add(Criteria.where("em").gte(de));
        }
        if (ate != null) {
            filtros.add(Criteria.where("em").lte(ate));
        }
        if (!filtros.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filtros.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, AuditoriaEvento.class);
        List<AuditoriaEvento> pagina = mongoTemplate.find(query.with(pageable), AuditoriaEvento.class);
        return PageableExecutionUtils.getPage(pagina, pageable, () -> total);
    }
}
