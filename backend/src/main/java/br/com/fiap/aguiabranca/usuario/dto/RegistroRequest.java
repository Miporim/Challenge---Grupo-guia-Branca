package br.com.fiap.aguiabranca.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do cadastro público. Sempre cria um OPERADOR — ver
 * {@code UsuarioService#criarOperador}.
 */
public record RegistroRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        // DECISION: senha mínima de 8 caracteres — não especificado na especificação.
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String senha
) {
}
