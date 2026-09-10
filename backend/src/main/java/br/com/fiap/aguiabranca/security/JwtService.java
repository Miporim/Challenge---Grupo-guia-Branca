package br.com.fiap.aguiabranca.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.fiap.aguiabranca.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Assinatura e leitura dos tokens JWT (HS256). Claims: {@code sub} = userId,
 * {@code email}, {@code role}, {@code iat}, {@code exp} — ver seção 5.
 */
@Component
public class JwtService {

    private final SecretKey chave;
    private final long horasValidade;

    public JwtService(
            @Value("${app.jwt.secret}") String segredo,
            @Value("${app.jwt.horas-validade}") long horasValidade) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.horasValidade = horasValidade;
    }

    public TokenGerado gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(horasValidade, ChronoUnit.HOURS);

        String token = Jwts.builder()
                .subject(usuario.getId())
                .claim("email", usuario.getEmail())
                .claim("role", usuario.getRole().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();

        return new TokenGerado(token, expiracao);
    }

    /** @throws JwtException se o token estiver expirado, malformado ou com assinatura inválida. */
    public Claims validarEExtrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenGerado(String token, Instant expiraEm) {
    }
}
