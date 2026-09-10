package br.com.fiap.aguiabranca.relatorio.dto;

import java.math.BigDecimal;

import br.com.fiap.aguiabranca.projeto.dto.ProjetoResponse;

public record ProjetoDetalheResponse(
        ProjetoResponse projeto,
        BigDecimal retornoTotal,
        BigDecimal lucro,
        BigDecimal roiPercentual
) {
}
