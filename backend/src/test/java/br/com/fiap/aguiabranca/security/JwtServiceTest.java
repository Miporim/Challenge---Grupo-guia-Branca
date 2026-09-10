package br.com.fiap.aguiabranca.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.fiap.aguiabranca.usuario.Role;
import br.com.fiap.aguiabranca.usuario.Usuario;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    // Segredo só para teste — não é usado em nenhum ambiente real.
    private final JwtService jwtService = new JwtService(
            "segredo-de-teste-com-mais-de-32-caracteres-para-hs256", 8);

    private final Usuario usuario = Usuario.builder()
            .id("usr-1")
            .nome("Ana Líder")
            .email("ana@aguiabranca.com")
            .role(Role.LIDER)
            .build();

    @Test
    void geraTokenComClaimsEsperados() {
        JwtService.TokenGerado gerado = jwtService.gerarToken(usuario);

        var claims = jwtService.validarEExtrairClaims(gerado.token());

        assertEquals("usr-1", claims.getSubject());
        assertEquals("ana@aguiabranca.com", claims.get("email", String.class));
        assertEquals("LIDER", claims.get("role", String.class));
    }

    @Test
    void expiraEmOitoHoras() {
        Instant antes = Instant.now();
        JwtService.TokenGerado gerado = jwtService.gerarToken(usuario);

        Instant esperado = antes.plus(Duration.ofHours(8));
        long diferencaSegundos = Math.abs(Duration.between(esperado, gerado.expiraEm()).getSeconds());
        assertTrue(diferencaSegundos <= 2, "expiraEm deveria ficar a 8h da geração");
    }

    @Test
    void tokenAdulteradoNaoValida() {
        JwtService.TokenGerado gerado = jwtService.gerarToken(usuario);
        String tokenAdulterado = gerado.token() + "adulterado";

        assertThrows(JwtException.class, () -> jwtService.validarEExtrairClaims(tokenAdulterado));
    }
}
