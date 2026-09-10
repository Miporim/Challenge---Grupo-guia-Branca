package br.com.fiap.aguiabranca.usuario.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEm,
        UsuarioResponse usuario
) {
}
