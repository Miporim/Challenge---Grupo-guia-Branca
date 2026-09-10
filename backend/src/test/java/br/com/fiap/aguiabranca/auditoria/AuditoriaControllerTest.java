package br.com.fiap.aguiabranca.auditoria;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.security.CorrelationIdFilter;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;

/**
 * Contrato HTTP de /api/auditoria — {@code AuditoriaService} mockado. A
 * matriz de acesso tem sua prova em
 * {@link AuditoriaServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(AuditoriaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditoriaService auditoriaService;

    @Test
    void listarComLiderRetorna200() throws Exception {
        when(auditoriaService.listar(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/auditoria").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }

    @Test
    void listarSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/auditoria"))
                .andExpect(status().isUnauthorized());
    }
}
