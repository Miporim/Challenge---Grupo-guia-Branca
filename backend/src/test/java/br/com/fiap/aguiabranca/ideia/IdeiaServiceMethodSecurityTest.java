package br.com.fiap.aguiabranca.ideia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.dto.IdeiaRequest;
import br.com.fiap.aguiabranca.ideia.dto.PriorizacaoRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Prova da matriz de acesso de {@link IdeiaService}, incluindo a posse
 * (autor) via {@link IdeiaSecurity} — mesmo padrão das etapas anteriores.
 *
 * DECISION: autentica manualmente com {@code UsernamePasswordAuthenticationToken}
 * (principal = String, igual ao {@code JwtAuthenticationFilter} real) em vez
 * de {@code @WithMockUser} — este usa um principal {@code UserDetails}, cujo
 * {@code toString()} não é o username puro, o que quebraria a comparação de
 * posse em {@link IdeiaSecurity#ehAutor}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IdeiaServiceMethodSecurityTest.SecurityTestConfig.class,
        IdeiaService.class,
        IdeiaSecurity.class
})
class IdeiaServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @MockitoBean
    private IdeiaRepository ideiaRepository;

    @MockitoBean
    private EstrategiaService estrategiaService;

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private AuditoriaPublisher auditoriaPublisher;

    @Autowired
    private IdeiaService ideiaService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuarioId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuarioId, null,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))));
    }

    private IdeiaRequest requestValido() {
        return new IdeiaRequest("Título", "Descrição", "estrategia-1", Nivel.ALTO, Nivel.BAIXO, null);
    }

    @Test
    void operadorConsegueCriar() {
        autenticarComo("operador-1", "OPERADOR");
        Estrategia vigente = Estrategia.builder().id("estrategia-1").status(EstrategiaStatus.VIGENTE).build();
        when(estrategiaService.buscarPorId("estrategia-1")).thenReturn(vigente);
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> ideiaService.criar(requestValido()));
    }

    @Test
    void gestorNaoConsegueCriar() {
        autenticarComo("gestor-1", "GESTOR");
        assertThrows(AccessDeniedException.class, () -> ideiaService.criar(requestValido()));
    }

    @Test
    void autorConsegueAtualizarAPropriaIdeia() {
        autenticarComo("operador-1", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));
        Estrategia vigente = Estrategia.builder().id("estrategia-1").status(EstrategiaStatus.VIGENTE).build();
        when(estrategiaService.buscarPorId("estrategia-1")).thenReturn(vigente);
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> ideiaService.atualizar("id-1", requestValido()));
    }

    @Test
    void outroOperadorNaoConsegueAtualizarIdeiaDeTerceiro() {
        autenticarComo("operador-2", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertThrows(AccessDeniedException.class, () -> ideiaService.atualizar("id-1", requestValido()));
    }

    @Test
    void gestorNaoConsegueAtualizarIdeiaDeOutrem() {
        autenticarComo("gestor-1", "GESTOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertThrows(AccessDeniedException.class, () -> ideiaService.atualizar("id-1", requestValido()));
    }

    @Test
    void autorConsegueExcluirAPropriaIdeia() {
        autenticarComo("operador-1", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertDoesNotThrow(() -> ideiaService.excluir("id-1"));
    }

    @Test
    void gestorConsegueExcluirIdeiaDeOutrem() {
        autenticarComo("gestor-1", "GESTOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertDoesNotThrow(() -> ideiaService.excluir("id-1"));
    }

    @Test
    void outroOperadorNaoConsegueExcluirIdeiaDeTerceiro() {
        autenticarComo("operador-2", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertThrows(AccessDeniedException.class, () -> ideiaService.excluir("id-1"));
    }

    @Test
    void gestorConseguePriorizar() {
        autenticarComo("gestor-1", "GESTOR");
        Ideia ideia = Ideia.builder().id("id-1").status(StatusIdeia.SUBMETIDA).priorizacoes(List.of()).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> ideiaService.priorizar("id-1", new PriorizacaoRequest(4, "ok")));
    }

    @Test
    void operadorNaoConseguePriorizar() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class,
                () -> ideiaService.priorizar("id-1", new PriorizacaoRequest(4, "ok")));
    }

    @Test
    void liderNaoConseguePriorizar() {
        autenticarComo("lider-1", "LIDER");
        assertThrows(AccessDeniedException.class,
                () -> ideiaService.priorizar("id-1", new PriorizacaoRequest(4, "ok")));
    }

    @Test
    void gestorELiderConseguemListar() {
        autenticarComo("gestor-1", "GESTOR");
        when(mongoTemplate.count(any(), org.mockito.ArgumentMatchers.eq(Ideia.class))).thenReturn(0L);
        when(mongoTemplate.find(any(), org.mockito.ArgumentMatchers.eq(Ideia.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> ideiaService.listar(null, null, PageRequest.of(0, 20)));
    }

    @Test
    void operadorNaoConsegueListarGeral() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class,
                () -> ideiaService.listar(null, null, PageRequest.of(0, 20)));
    }

    @Test
    void operadorConsegueListarMinhas() {
        autenticarComo("operador-1", "OPERADOR");
        when(ideiaRepository.findByAutorId("operador-1", PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        assertDoesNotThrow(() -> ideiaService.listarMinhas(PageRequest.of(0, 20)));
    }

    @Test
    void gestorNaoConsegueListarMinhas() {
        autenticarComo("gestor-1", "GESTOR");
        assertThrows(AccessDeniedException.class, () -> ideiaService.listarMinhas(PageRequest.of(0, 20)));
    }

    @Test
    void autorConsegueVisualizarAPropriaIdeia() {
        autenticarComo("operador-1", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertDoesNotThrow(() -> ideiaService.buscarParaVisualizacao("id-1"));
    }

    @Test
    void gestorConsegueVisualizarIdeiaDeOutrem() {
        autenticarComo("gestor-1", "GESTOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertDoesNotThrow(() -> ideiaService.buscarParaVisualizacao("id-1"));
    }

    @Test
    void outroOperadorNaoConsegueVisualizarIdeiaDeTerceiro() {
        autenticarComo("operador-2", "OPERADOR");
        Ideia ideia = Ideia.builder().id("id-1").autorId("operador-1").build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(ideia));

        assertThrows(AccessDeniedException.class, () -> ideiaService.buscarParaVisualizacao("id-1"));
    }
}
