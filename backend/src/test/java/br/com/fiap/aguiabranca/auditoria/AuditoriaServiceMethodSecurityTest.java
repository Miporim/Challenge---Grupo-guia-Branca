package br.com.fiap.aguiabranca.auditoria;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AuditoriaServiceMethodSecurityTest.SecurityTestConfig.class,
        AuditoriaService.class
})
class AuditoriaServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
    }

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuditoriaService auditoriaService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuarioId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void liderConsegueListar() {
        autenticarComo("lider-1", "LIDER");
        when(mongoTemplate.count(any(), org.mockito.ArgumentMatchers.eq(AuditoriaEvento.class))).thenReturn(0L);
        when(mongoTemplate.find(any(), org.mockito.ArgumentMatchers.eq(AuditoriaEvento.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> auditoriaService.listar(null, null, null, null, null, PageRequest.of(0, 20)));
    }

    @Test
    void gestorNaoConsegueListar() {
        autenticarComo("gestor-1", "GESTOR");
        assertThrows(AccessDeniedException.class,
                () -> auditoriaService.listar(null, null, null, null, null, PageRequest.of(0, 20)));
    }

    @Test
    void operadorNaoConsegueListar() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class,
                () -> auditoriaService.listar(null, null, null, null, null, PageRequest.of(0, 20)));
    }
}
