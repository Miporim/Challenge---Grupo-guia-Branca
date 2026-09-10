package br.com.fiap.aguiabranca.ia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.ia.dto.ChatRequest;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.relatorio.RelatorioService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Prova da matriz de acesso de {@link IaService}, incluindo a posse da
 * conversa via {@link ConversaSecurity} — mesmo padrão de
 * {@code IdeiaSecurity} (Etapa C).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        IaServiceMethodSecurityTest.SecurityTestConfig.class,
        IaService.class,
        ConversaSecurity.class
})
class IaServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        PromptLoader promptLoader() {
            return new PromptLoader();
        }

        @Bean
        InsightsCache insightsCache() {
            return new InsightsCache();
        }
    }

    @MockitoBean
    private IdeiaService ideiaService;

    @MockitoBean
    private EstrategiaService estrategiaService;

    @MockitoBean
    private ConversaIaRepository conversaIaRepository;

    @MockitoBean
    private IaClient iaClient;

    @MockitoBean
    private AuditoriaPublisher auditoriaPublisher;

    @MockitoBean
    private RelatorioService relatorioService;

    @Autowired
    private IaService iaService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuarioId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void operadorNaoConsegueAnalisarIdeia() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> iaService.analisarIdeia("ideia-1"));
    }

    @Test
    void liderNaoConsegueAnalisarIdeia() {
        autenticarComo("lider-1", "LIDER");
        assertThrows(AccessDeniedException.class, () -> iaService.analisarIdeia("ideia-1"));
    }

    @Test
    void operadorNaoConsegueAnalisarLote() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> iaService.analisarLote("estrategia-1"));
    }

    @Test
    void operadorNaoConsegueUsarChat() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> iaService.chat(new ChatRequest(null, "oi")));
    }

    @Test
    void gestorNaoConsegueAbrirConversaDeOutroGestor() {
        autenticarComo("gestor-2", "GESTOR");
        ConversaIa conversaDeOutro = ConversaIa.builder().id("conversa-1").gestorId("gestor-1").mensagens(List.of()).build();
        when(conversaIaRepository.findById("conversa-1")).thenReturn(Optional.of(conversaDeOutro));

        assertThrows(AccessDeniedException.class,
                () -> iaService.chat(new ChatRequest("conversa-1", "continuando")));
    }

    @Test
    void gestorConsegueContinuarAPropriaConversa() {
        autenticarComo("gestor-1", "GESTOR");
        ConversaIa propria = ConversaIa.builder().id("conversa-1").gestorId("gestor-1")
                .mensagens(new java.util.ArrayList<>()).build();
        when(conversaIaRepository.findById("conversa-1")).thenReturn(Optional.of(propria));
        when(estrategiaService.vigenteAtual()).thenReturn(
                br.com.fiap.aguiabranca.estrategia.Estrategia.builder()
                        .id("estrategia-1").titulo("T").descricao("D")
                        .categoria(br.com.fiap.aguiabranca.estrategia.Categoria.EFICIENCIA).build());
        when(ideiaService.listarSubmetidasPorEstrategia(any())).thenReturn(List.of());
        when(iaClient.gerarTexto(anyString())).thenReturn("resposta");
        when(conversaIaRepository.save(any(ConversaIa.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> iaService.chat(new ChatRequest("conversa-1", "continuando")));
    }

    @Test
    void gestorConsegueBuscarAPropriaConversa() {
        autenticarComo("gestor-1", "GESTOR");
        ConversaIa propria = ConversaIa.builder().id("conversa-1").gestorId("gestor-1").mensagens(List.of()).build();
        when(conversaIaRepository.findById("conversa-1")).thenReturn(Optional.of(propria));

        assertDoesNotThrow(() -> iaService.buscarConversa("conversa-1"));
    }

    @Test
    void gestorNaoConsegueBuscarConversaDeOutroGestor() {
        autenticarComo("gestor-2", "GESTOR");
        ConversaIa deOutro = ConversaIa.builder().id("conversa-1").gestorId("gestor-1").mensagens(List.of()).build();
        when(conversaIaRepository.findById("conversa-1")).thenReturn(Optional.of(deOutro));

        assertThrows(AccessDeniedException.class, () -> iaService.buscarConversa("conversa-1"));
    }

    @Test
    void liderConsegueVerInsights() {
        autenticarComo("lider-1", "LIDER");
        when(relatorioService.resumo()).thenReturn(new br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse(
                0, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.util.Map.of(), java.util.Map.of()));
        when(relatorioService.porEstrategia()).thenReturn(List.of());
        when(iaClient.gerarTexto(anyString())).thenReturn(
                "{\"leituraGeral\": \"ok\", \"pontosAtencao\": [\"a\"], \"recomendacoes\": [\"b\"]}");

        assertDoesNotThrow(iaService::insights);
    }

    @Test
    void gestorNaoConsegueVerInsights() {
        autenticarComo("gestor-1", "GESTOR");
        assertThrows(AccessDeniedException.class, iaService::insights);
    }
}
