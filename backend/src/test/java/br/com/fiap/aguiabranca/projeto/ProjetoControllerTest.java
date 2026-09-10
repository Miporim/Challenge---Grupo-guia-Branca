package br.com.fiap.aguiabranca.projeto;

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
import br.com.fiap.aguiabranca.projeto.dto.AtualizarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.CriarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProgressoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ResultadoRequest;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Contrato HTTP de /api/projetos — {@code ProjetoService} mockado. A
 * matriz de acesso tem sua prova em
 * {@link ProjetoServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(ProjetoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class ProjetoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ProjetoService projetoService;

    private Projeto projetoExemplo() {
        return Projeto.builder()
                .id("id-1")
                .titulo("Título")
                .descricao("Descrição")
                .estrategiaId("estrategia-1")
                .gestorId("gestor-1")
                .etapa(EtapaProjeto.PLANEJAMENTO)
                .status(StatusProjeto.NO_PRAZO)
                .investimento(BigDecimal.TEN)
                .prazoInicio(Instant.now())
                .prazoFim(Instant.now().plusSeconds(3600))
                .percentualConcluido(0)
                .resultados(java.util.List.of())
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();
    }

    @Test
    void criarComSucessoRetorna201() throws Exception {
        when(projetoService.criar(any())).thenReturn(projetoExemplo());
        CriarProjetoRequest request = new CriarProjetoRequest("T", "D", "estrategia-1", null,
                BigDecimal.TEN, Instant.now(), Instant.now().plusSeconds(3600), null, null);

        mockMvc.perform(post("/api/projetos")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void criarSemTokenRetorna401() throws Exception {
        CriarProjetoRequest request = new CriarProjetoRequest("T", "D", "estrategia-1", null,
                BigDecimal.TEN, Instant.now(), Instant.now().plusSeconds(3600), null, null);

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarRetorna200() throws Exception {
        when(projetoService.listar(any(), any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/projetos").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorIdRetorna200() throws Exception {
        when(projetoService.buscarPorId("id-1")).thenReturn(projetoExemplo());

        mockMvc.perform(get("/api/projetos/id-1").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(projetoService.atualizar(eq("id-1"), any())).thenReturn(projetoExemplo());
        AtualizarProjetoRequest request = new AtualizarProjetoRequest("T", "D", "estrategia-1",
                BigDecimal.TEN, Instant.now(), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/projetos/id-1")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarProgressoRetorna200() throws Exception {
        when(projetoService.atualizarProgresso(eq("id-1"), any())).thenReturn(projetoExemplo());

        mockMvc.perform(patch("/api/projetos/id-1/progresso")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProgressoRequest(null, null, 50))))
                .andExpect(status().isOk());
    }

    @Test
    void registrarResultadoRetorna200() throws Exception {
        when(projetoService.registrarResultado(eq("id-1"), any())).thenReturn(projetoExemplo());
        ResultadoRequest request = new ResultadoRequest(Instant.now(), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, "obs");

        mockMvc.perform(post("/api/projetos/id-1/resultados")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/api/projetos/id-1").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isNoContent());
    }
}
