package br.com.fiap.aguiabranca.ideia.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.ideia.Priorizacao;

public record PriorizacaoResponse(
        String gestorId,
        int nota,
        String comentario,
        Instant em
) {
    public static PriorizacaoResponse de(Priorizacao priorizacao) {
        return new PriorizacaoResponse(
                priorizacao.getGestorId(),
                priorizacao.getNota(),
                priorizacao.getComentario(),
                priorizacao.getEm()
        );
    }
}
