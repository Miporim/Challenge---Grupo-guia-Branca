package br.com.fiap.aguiabranca.estrategia.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.estrategia.Categoria;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Usado tanto na criação ({@code POST}) quanto na atualização
 * ({@code PUT}) — os mesmos campos em ambos os casos.
 */
public record EstrategiaRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Categoria é obrigatória")
        Categoria categoria,

        @NotBlank(message = "Campanha é obrigatória")
        String campanha,

        @NotNull(message = "Vigência início é obrigatória")
        Instant vigenciaInicio,

        Instant vigenciaFim,

        // DECISION: opcional — se omitido, a estratégia é criada/atualizada
        // como VIGENTE. Não especificado explicitamente na seção 4.
        EstrategiaStatus status
) {
}
