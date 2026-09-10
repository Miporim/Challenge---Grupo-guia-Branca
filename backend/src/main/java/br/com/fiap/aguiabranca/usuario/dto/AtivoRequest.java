package br.com.fiap.aguiabranca.usuario.dto;

import jakarta.validation.constraints.NotNull;

// DECISION: corpo de PATCH /api/usuarios/{id}/ativo — não especificado no
// documento, optei pela forma mais simples: { "ativo": true|false }.
public record AtivoRequest(
        @NotNull(message = "Campo ativo é obrigatório")
        Boolean ativo
) {
}
