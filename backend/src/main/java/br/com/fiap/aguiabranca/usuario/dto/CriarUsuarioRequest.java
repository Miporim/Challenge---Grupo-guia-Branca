package br.com.fiap.aguiabranca.usuario.dto;

import br.com.fiap.aguiabranca.usuario.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code POST /api/usuarios} (só LIDER). Cria GESTOR ou LIDER —
 * OPERADOR não é aceito aqui, só via cadastro público.
 */
public record CriarUsuarioRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotNull(message = "Role é obrigatório")
        Role role
) {
}
