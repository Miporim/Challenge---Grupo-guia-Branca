package br.com.fiap.aguiabranca.ia.dto;

import jakarta.validation.constraints.NotBlank;

public record AnaliseLoteRequest(
        @NotBlank(message = "Estratégia é obrigatória")
        String estrategiaId
) {
}
