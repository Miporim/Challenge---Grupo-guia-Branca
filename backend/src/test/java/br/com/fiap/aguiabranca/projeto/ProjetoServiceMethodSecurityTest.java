package br.com.fiap.aguiabranca.projeto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.projeto.dto.CriarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProgressoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ResultadoRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Prova da matriz de acesso de {@link ProjetoService} — só role (sem
 * posse: a seção 4 não restringe as operações de projeto ao gestor
 * autor, qualquer GESTOR pode agir sobre qualquer projeto).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ProjetoServiceMethodSecurityTest.SecurityTestConfig.class,
        ProjetoService.class
})
class ProjetoServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @MockitoBean
    private ProjetoRepository projetoRepository;

    @MockitoBean
    private EstrategiaService estrategiaService;

    @MockitoBean
    private IdeiaService ideiaService;

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private AuditoriaPublisher auditoriaPublisher;

    @Autowired
    private ProjetoService projetoService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuarioId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private CriarProjetoRequest requestValido() {
        return new CriarProjetoRequest("T", "D", "estrategia-1", null,
                BigDecimal.TEN, Instant.now(), Instant.now().plusSeconds(3600), null, null);
    }

    @Test
    void gestorConsegueCriar() {
        autenticarComo("gestor-1", "GESTOR");
        when(estrategiaService.buscarPorId("estrategia-1"))
                .thenReturn(Estrategia.builder().id("estrategia-1").status(EstrategiaStatus.VIGENTE).build());
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> projetoService.criar(requestValido()));
    }

    @Test
    void operadorNaoConsegueCriar() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> projetoService.criar(requestValido()));
    }

    @Test
    void liderNaoConsegueCriar() {
        autenticarComo("lider-1", "LIDER");
        assertThrows(AccessDeniedException.class, () -> projetoService.criar(requestValido()));
    }

    @Test
    void operadorNaoConsegueAtualizarProgresso() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class,
                () -> projetoService.atualizarProgresso("id-1", new ProgressoRequest(null, null, 50)));
    }

    @Test
    void gestorConsegueAtualizarProgresso() {
        autenticarComo("gestor-1", "GESTOR");
        Projeto projeto = Projeto.builder().id("id-1").build();
        when(projetoRepository.findById("id-1")).thenReturn(Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> projetoService.atualizarProgresso("id-1", new ProgressoRequest(null, null, 50)));
    }

    @Test
    void operadorNaoConsegueRegistrarResultado() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> projetoService.registrarResultado("id-1",
                new ResultadoRequest(Instant.now(), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, "x")));
    }

    @Test
    void operadorNaoConsegueExcluir() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> projetoService.excluir("id-1"));
    }

    @Test
    void gestorConsegueExcluir() {
        autenticarComo("gestor-1", "GESTOR");
        Projeto projeto = Projeto.builder().id("id-1").build();
        when(projetoRepository.findById("id-1")).thenReturn(Optional.of(projeto));

        assertDoesNotThrow(() -> projetoService.excluir("id-1"));
    }
}
