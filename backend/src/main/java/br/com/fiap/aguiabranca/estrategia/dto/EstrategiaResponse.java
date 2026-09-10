package br.com.fiap.aguiabranca.estrategia.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.estrategia.Categoria;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;

public record EstrategiaResponse(
        String id,
        String titulo,
        String descricao,
        Categoria categoria,
        String campanha,
        Instant vigenciaInicio,
        Instant vigenciaFim,
        EstrategiaStatus status,
        String criadoPor,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static EstrategiaResponse de(Estrategia estrategia) {
        return new EstrategiaResponse(
                estrategia.getId(),
                estrategia.getTitulo(),
                estrategia.getDescricao(),
                estrategia.getCategoria(),
                estrategia.getCampanha(),
                estrategia.getVigenciaInicio(),
                estrategia.getVigenciaFim(),
                estrategia.getStatus(),
                estrategia.getCriadoPor(),
                estrategia.getCriadoEm(),
                estrategia.getAtualizadoEm()
        );
    }
}
