package br.com.fiap.aguiabranca.auditoria.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.auditoria.Acao;
import br.com.fiap.aguiabranca.auditoria.AuditoriaEvento;

public record AuditoriaResponse(
        String id,
        AtorResponse ator,
        Acao acao,
        String recurso,
        String recursoId,
        AlteracoesResponse alteracoes,
        String ip,
        String correlationId,
        Instant em
) {
    public static AuditoriaResponse de(AuditoriaEvento evento) {
        return new AuditoriaResponse(
                evento.getId(),
                AtorResponse.de(evento.getAtor()),
                evento.getAcao(),
                evento.getRecurso(),
                evento.getRecursoId(),
                AlteracoesResponse.de(evento.getAlteracoes()),
                evento.getIp(),
                evento.getCorrelationId(),
                evento.getEm()
        );
    }
}
