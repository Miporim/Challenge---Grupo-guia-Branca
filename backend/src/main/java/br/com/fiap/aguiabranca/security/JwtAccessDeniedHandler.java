package br.com.fiap.aguiabranca.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 403 — role ou posse insuficiente. Cobre tanto a regra grossa de rota
 * quanto o {@code @PreAuthorize} dos services (a {@link AccessDeniedException}
 * lançada lá sobe pela cadeia e é capturada pelo
 * {@code ExceptionTranslationFilter}, chegando aqui).
 *
 * DECISION: {@code ObjectMapper} próprio, mesmo motivo do
 * {@link JwtAuthenticationEntryPoint}.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problema.setTitle("Acesso negado");
        problema.setDetail("Você não tem permissão para executar esta operação");

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problema);
    }
}
