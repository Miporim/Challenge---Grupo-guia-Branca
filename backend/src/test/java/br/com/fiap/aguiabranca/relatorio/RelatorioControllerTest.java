package br.com.fiap.aguiabranca.relatorio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.relatorio.dto.FunilResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ProjetoDetalheResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse;
import br.com.fiap.aguiabranca.relatorio.dto.SerieTemporalItem;
import br.com.fiap.aguiabranca.projeto.dto.ProjetoResponse;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;

/**
 * Contrato HTTP de /api/relatorios — {@code RelatorioService} mockado. A
 * matriz de acesso tem sua prova em
 * {@link RelatorioServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(RelatorioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService relatorioService;

    @Test
    void resumoRetorna200() throws Exception {
        when(relatorioService.resumo()).thenReturn(new ResumoResponse(
                0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, Map.of(), Map.of()));

        mockMvc.perform(get("/api/relatorios/resumo").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void resumoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/relatorios/resumo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void porEstrategiaRetorna200() throws Exception {
        when(relatorioService.porEstrategia()).thenReturn(List.of());

        mockMvc.perform(get("/api/relatorios/por-estrategia").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }

    @Test
    void projetoDetalheRetorna200() throws Exception {
        ProjetoResponse projeto = new ProjetoResponse("id-1", "T", "D", "estrategia-1", null, "gestor-1",
                null, null, BigDecimal.TEN, Instant.now(), Instant.now(), null, 0, List.of(), Instant.now(), Instant.now());
        when(relatorioService.projetoDetalhe("id-1")).thenReturn(
                new ProjetoDetalheResponse(projeto, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        mockMvc.perform(get("/api/relatorios/projetos/id-1").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void serieTemporalRetorna200() throws Exception {
        when(relatorioService.serieTemporal(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/relatorios/serie-temporal")
                        .param("de", "2026-01-01T00:00:00Z")
                        .param("ate", "2026-12-31T00:00:00Z")
                        .with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }

    @Test
    void funilRetorna200() throws Exception {
        when(relatorioService.funil()).thenReturn(new FunilResponse(0, 0, 0, 0));

        mockMvc.perform(get("/api/relatorios/funil").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }
}
