package br.com.fiap.aguiabranca.auditoria.dto;

import java.util.Map;

import br.com.fiap.aguiabranca.auditoria.Alteracoes;

public record AlteracoesResponse(Map<String, Object> antes, Map<String, Object> depois) {
    public static AlteracoesResponse de(Alteracoes alteracoes) {
        if (alteracoes == null) {
            return null;
        }
        return new AlteracoesResponse(alteracoes.getAntes(), alteracoes.getDepois());
    }
}
