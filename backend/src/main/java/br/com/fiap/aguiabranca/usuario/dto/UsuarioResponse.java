package br.com.fiap.aguiabranca.usuario.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.usuario.Role;
import br.com.fiap.aguiabranca.usuario.Usuario;

/**
 * Nunca inclui {@code senhaHash} — nenhuma resposta pode expor a senha.
 */
public record UsuarioResponse(
        String id,
        String nome,
        String email,
        Role role,
        boolean ativo,
        Instant criadoEm
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.isAtivo(),
                usuario.getCriadoEm()
        );
    }
}
