package br.com.fiap.aguiabranca.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.fiap.aguiabranca.usuario.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gera um {@code correlationId} por requisição e o põe no MDC junto com
 * {@code userId}/{@code role} — seção 6. Roda DEPOIS do
 * {@code JwtAuthenticationFilter} (ver {@code SecurityConfig}) para que o
 * {@code SecurityContext} já esteja povoado quando lê o usuário.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String correlationId = UUID.randomUUID().toString();
        try {
            MDC.put("correlationId", correlationId);

            String usuarioId = SecurityUtils.getUsuarioId();
            if (usuarioId != null) {
                MDC.put("userId", usuarioId);
            }
            Role role = SecurityUtils.getRole();
            if (role != null) {
                MDC.put("role", role.name());
            }

            response.setHeader(HEADER_CORRELATION_ID, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
