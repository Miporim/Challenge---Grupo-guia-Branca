package br.com.fiap.aguiabranca.ideia.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiabranca.ideia.AnaliseIa;
import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.Nivel;
import br.com.fiap.aguiabranca.ideia.StatusIdeia;

public record IdeiaResponse(
        String id,
        String titulo,
        String descricao,
        String autorId,
        String estrategiaId,
        StatusIdeia status,
        Nivel impacto,
        Nivel esforco,
        List<PriorizacaoResponse> priorizacoes,
        BigDecimal notaMedia,
        int totalVotos,
        AnaliseIa analiseIa,
        String projetoId,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static IdeiaResponse de(Ideia ideia) {
        List<PriorizacaoResponse> priorizacoes = ideia.getPriorizacoes() == null
                ? List.of()
                : ideia.getPriorizacoes().stream().map(PriorizacaoResponse::de).toList();

        return new IdeiaResponse(
                ideia.getId(),
                ideia.getTitulo(),
                ideia.getDescricao(),
                ideia.getAutorId(),
                ideia.getEstrategiaId(),
                ideia.getStatus(),
                ideia.getImpacto(),
                ideia.getEsforco(),
                priorizacoes,
                ideia.getNotaMedia() == null ? BigDecimal.ZERO : ideia.getNotaMedia(),
                ideia.getTotalVotos(),
                ideia.getAnaliseIa(),
                ideia.getProjetoId(),
                ideia.getCriadoEm(),
                ideia.getAtualizadoEm()
        );
    }
}
