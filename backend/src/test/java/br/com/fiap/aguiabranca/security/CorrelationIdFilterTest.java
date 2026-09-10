package br.com.fiap.aguiabranca.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filtro = new CorrelationIdFilter();

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void geraCorrelationIdEHeaderNaResposta() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, new MockFilterChain());

        assertNotNull(response.getHeader("X-Correlation-Id"));
    }

    @Test
    void limpaMdcAoFinalDaRequisicao() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "usr-1", null, List.of(new SimpleGrantedAuthority("ROLE_LIDER"))));

        filtro.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("role"));
    }
}
