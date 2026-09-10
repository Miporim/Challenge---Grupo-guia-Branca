package br.com.fiap.aguiabranca.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.Projeto;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;

public record ProjetoResponse(
        String id,
        String titulo,
        String descricao,
        String estrategiaId,
        String ideiaOrigemId,
        String gestorId,
        EtapaProjeto etapa,
        StatusProjeto status,
        BigDecimal investimento,
        Instant prazoInicio,
        Instant prazoFim,
        Instant concluidoEm,
        int percentualConcluido,
        List<ResultadoResponse> resultados,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ProjetoResponse de(Projeto projeto) {
        List<ResultadoResponse> resultados = projeto.getResultados() == null
                ? List.of()
                : projeto.getResultados().stream().map(ResultadoResponse::de).toList();

        return new ProjetoResponse(
                projeto.getId(),
                projeto.getTitulo(),
                projeto.getDescricao(),
                projeto.getEstrategiaId(),
                projeto.getIdeiaOrigemId(),
                projeto.getGestorId(),
                projeto.getEtapa(),
                projeto.getStatus(),
                projeto.getInvestimento(),
                projeto.getPrazoInicio(),
                projeto.getPrazoFim(),
                projeto.getConcluidoEm(),
                projeto.getPercentualConcluido(),
                resultados,
                projeto.getCriadoEm(),
                projeto.getAtualizadoEm()
        );
    }
}
