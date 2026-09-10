package br.com.fiap.aguiabranca.ia.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code conversaId} nulo cria uma nova conversa. */
public record ChatRequest(
        String conversaId,

        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem
) {
}
