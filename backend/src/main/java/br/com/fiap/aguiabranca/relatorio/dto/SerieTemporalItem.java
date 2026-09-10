package br.com.fiap.aguiabranca.relatorio.dto;

import java.math.BigDecimal;

/** {@code periodo} no formato {@code AAAA-MM} (única granularidade suportada — ver {@code RelatorioService}). */
public record SerieTemporalItem(
        String periodo,
        BigDecimal investimento,
        BigDecimal retorno
) {
}
