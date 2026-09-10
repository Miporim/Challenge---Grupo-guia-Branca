package br.com.fiap.aguiabranca.ia.dto;

import java.util.List;

public record InsightsResponse(
        String leituraGeral,
        List<String> pontosAtencao,
        List<String> recomendacoes
) {
}
