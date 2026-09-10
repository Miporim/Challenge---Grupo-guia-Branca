package br.com.fiap.aguiabranca.relatorio.dto;

import java.math.BigDecimal;
import java.util.Map;

import br.com.fiap.aguiabranca.estrategia.Categoria;
import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;

public record PorEstrategiaResponse(
        String estrategiaId,
        String titulo,
        Categoria categoria,
        long totalProjetos,
        BigDecimal investimentoTotal,
        BigDecimal retornoTotal,
        BigDecimal lucro,
        BigDecimal roiPercentual,
        BigDecimal prazoMedioDias,
        BigDecimal ganhoProdutividadeMedio,
        Map<EtapaProjeto, Long> porEtapa,
        Map<StatusProjeto, Long> porStatus
) {
}
