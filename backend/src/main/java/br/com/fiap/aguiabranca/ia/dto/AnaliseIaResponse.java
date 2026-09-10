package br.com.fiap.aguiabranca.ia.dto;

import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiabranca.ideia.AnaliseIa;

public record AnaliseIaResponse(
        int score,
        int aderenciaEstrategia,
        String justificativa,
        List<String> riscos,
        String modelo,
        Instant geradoEm
) {
    public static AnaliseIaResponse de(AnaliseIa analise) {
        return new AnaliseIaResponse(
                analise.getScore(),
                analise.getAderenciaEstrategia(),
                analise.getJustificativa(),
                analise.getRiscos(),
                analise.getModelo(),
                analise.getGeradoEm()
        );
    }
}
