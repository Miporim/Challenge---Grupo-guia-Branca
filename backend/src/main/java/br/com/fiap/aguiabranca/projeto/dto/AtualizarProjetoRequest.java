package br.com.fiap.aguiabranca.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * PUT não toca {@code etapa}/{@code status}/{@code percentualConcluido}
 * (isso é {@code PATCH /progresso}) nem {@code ideiaOrigemId} (só se
 * define na criação) — campos "de cadastro" apenas.
 */
public record AtualizarProjetoRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Estratégia é obrigatória")
        String estrategiaId,

        @NotNull(message = "Investimento é obrigatório")
        @DecimalMin(value = "0", message = "Investimento não pode ser negativo")
        BigDecimal investimento,

        @NotNull(message = "Prazo início é obrigatório")
        Instant prazoInicio,

        @NotNull(message = "Prazo fim é obrigatório")
        Instant prazoFim
) {
}
