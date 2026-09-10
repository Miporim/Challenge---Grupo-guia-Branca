package br.com.fiap.aguiabranca.auditoria.dto;

import br.com.fiap.aguiabranca.auditoria.Ator;

public record AtorResponse(String userId, String email, String role) {
    public static AtorResponse de(Ator ator) {
        if (ator == null) {
            return null;
        }
        return new AtorResponse(ator.getUserId(), ator.getEmail(), ator.getRole());
    }
}
