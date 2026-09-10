package br.com.fiap.aguiabranca.ia.dto;

import java.util.List;

/**
 * Forma esperada do JSON que a IA devolve para pontuação de ideia
 * (seção 7.1) — usada tanto para parsear/validar a resposta quanto como
 * corpo de {@code POST /api/ia/ideias/{id}/analise} (sem
 * {@code modelo}/{@code geradoEm}, que são metadados do lado do
 * servidor, não da resposta da IA).
 */
public record PontuacaoIaResposta(
        Integer score,
        Integer aderenciaEstrategia,
        String justificativa,
        List<String> riscos
) {
}
