package br.com.fiap.aguiabranca.ideia.dto;

import br.com.fiap.aguiabranca.ideia.Nivel;
import br.com.fiap.aguiabranca.ideia.StatusIdeia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Usado tanto na criação quanto na atualização. {@code status}, se
 * informado, só aceita {@code RASCUNHO} ou {@code SUBMETIDA} — a
 * transição para os demais estados passa pelos endpoints dedicados
 * (priorização e {@code PATCH /status}).
 */
public record IdeiaRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Estratégia é obrigatória")
        String estrategiaId,

        // DECISION: impacto/esforco tratados como obrigatórios — a seção 3
        // não marca a coluna "obligatorios" para eles, mas não há valor
        // default sensato para uma classificação de esforço/impacto.
        @NotNull(message = "Impacto é obrigatório")
        Nivel impacto,

        @NotNull(message = "Esforço é obrigatório")
        Nivel esforco,

        StatusIdeia status
) {
}
