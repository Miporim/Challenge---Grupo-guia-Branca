package br.com.fiap.aguiabranca.ideia;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.ideia.dto.IdeiaRequest;
import br.com.fiap.aguiabranca.ideia.dto.PriorizacaoRequest;
import br.com.fiap.aguiabranca.ideia.dto.StatusIdeiaRequest;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.CorrelationIdFilter;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Contrato HTTP de /api/ideias — {@code IdeiaService} mockado. A matriz de
 * acesso e a posse (autor) têm sua prova em
 * {@link IdeiaServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(IdeiaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class IdeiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private IdeiaService ideiaService;

    private Ideia ideiaExemplo() {
        return Ideia.builder()
                .id("id-1")
                .titulo("Título")
                .descricao("Descrição")
                .autorId("operador-1")
                .estrategiaId("estrategia-1")
                .status(StatusIdeia.SUBMETIDA)
                .impacto(Nivel.ALTO)
                .esforco(Nivel.BAIXO)
                .priorizacoes(java.util.List.of())
                .notaMedia(BigDecimal.ZERO)
                .totalVotos(0)
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();
    }

    private IdeiaRequest requestValido() {
        return new IdeiaRequest("Título", "Descrição", "estrategia-1", Nivel.ALTO, Nivel.BAIXO, null);
    }

    @Test
    void criarComSucessoRetorna201() throws Exception {
        when(ideiaService.criar(any())).thenReturn(ideiaExemplo());

        mockMvc.perform(post("/api/ideias")
                        .with(user("operador-1").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated());
    }

    @Test
    void criarSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarSemEstrategiaIdRetorna400() throws Exception {
        // Seção 8, teste #3: "Ideia sin estrategiaId → 400".
        IdeiaRequest semEstrategia = new IdeiaRequest("Título", "Descrição", null, Nivel.ALTO, Nivel.BAIXO, null);

        mockMvc.perform(post("/api/ideias")
                        .with(user("operador-1").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semEstrategia)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarMinhasRetorna200() throws Exception {
        when(ideiaService.listarMinhas(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/ideias/minhas").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void listarRetorna200() throws Exception {
        when(ideiaService.listar(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/ideias").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorIdRetorna200() throws Exception {
        when(ideiaService.buscarParaVisualizacao("id-1")).thenReturn(ideiaExemplo());

        mockMvc.perform(get("/api/ideias/id-1").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(ideiaService.atualizar(eq("id-1"), any())).thenReturn(ideiaExemplo());

        mockMvc.perform(put("/api/ideias/id-1")
                        .with(user("operador-1").roles("OPERADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/api/ideias/id-1").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    void priorizarRetorna200() throws Exception {
        when(ideiaService.priorizar(eq("id-1"), any())).thenReturn(ideiaExemplo());

        mockMvc.perform(post("/api/ideias/id-1/priorizacao")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PriorizacaoRequest(4, "bom"))))
                .andExpect(status().isOk());
    }

    @Test
    void decidirRetorna200() throws Exception {
        when(ideiaService.decidir(eq("id-1"), any())).thenReturn(ideiaExemplo());

        mockMvc.perform(patch("/api/ideias/id-1/status")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusIdeiaRequest(StatusIdeia.APROVADA, "ok"))))
                .andExpect(status().isOk());
    }
}
