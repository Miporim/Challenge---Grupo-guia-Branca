package br.com.fiap.aguiabranca.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.fiap.aguiabranca.projeto.Resultado;

public record ResultadoResponse(
        Instant data,
        BigDecimal receita,
        BigDecimal economia,
        BigDecimal ganhoProdutividadePct,
        String observacao,
        String registradoPor
) {
    public static ResultadoResponse de(Resultado resultado) {
        return new ResultadoResponse(
                resultado.getData(),
                resultado.getReceita(),
                resultado.getEconomia(),
                resultado.getGanhoProdutividadePct(),
                resultado.getObservacao(),
                resultado.getRegistradoPor()
        );
    }
}
