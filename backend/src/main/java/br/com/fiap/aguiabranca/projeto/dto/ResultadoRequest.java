package br.com.fiap.aguiabranca.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ResultadoRequest(

        @NotNull(message = "Data é obrigatória")
        Instant data,

        @NotNull(message = "Receita é obrigatória")
        @DecimalMin(value = "0", message = "Receita não pode ser negativa")
        BigDecimal receita,

        @NotNull(message = "Economia é obrigatória")
        @DecimalMin(value = "0", message = "Economia não pode ser negativa")
        BigDecimal economia,

        @NotNull(message = "Ganho de produtividade é obrigatório")
        BigDecimal ganhoProdutividadePct,

        String observacao
) {
}
