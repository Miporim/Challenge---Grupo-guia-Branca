package br.com.fiap.aguiabranca.estrategia;

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
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaRequest;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Contrato HTTP de /api/estrategias. {@code EstrategiaService} é mockado —
 * a matriz de acesso tem sua prova em
 * {@link EstrategiaServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(EstrategiaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class EstrategiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private EstrategiaService estrategiaService;

    private Estrategia estrategiaExemplo() {
        return Estrategia.builder()
                .id("id-1")
                .titulo("Título")
                .descricao("Descrição")
                .categoria(Categoria.EFICIENCIA)
                .campanha("Ciclo 2026/1")
                .vigenciaInicio(Instant.now())
                .status(EstrategiaStatus.VIGENTE)
                .criadoPor("lider-1")
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();
    }

    private EstrategiaRequest requestValido() {
        return new EstrategiaRequest("Título", "Descrição", Categoria.EFICIENCIA, "Ciclo 2026/1",
                Instant.now(), Instant.now().plusSeconds(3600), EstrategiaStatus.VIGENTE);
    }

    @Test
    void criarComSucessoRetorna201() throws Exception {
        when(estrategiaService.criar(any())).thenReturn(estrategiaExemplo());

        mockMvc.perform(post("/api/estrategias")
                        .with(user("lider-1").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated());
    }

    @Test
    void criarSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/estrategias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarRetorna200() throws Exception {
        when(estrategiaService.listar(any(), any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/estrategias").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void vigenteRetorna200() throws Exception {
        when(estrategiaService.vigenteAtual()).thenReturn(estrategiaExemplo());

        mockMvc.perform(get("/api/estrategias/vigente").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void vigenteSemEstrategiaRetorna404() throws Exception {
        when(estrategiaService.vigenteAtual()).thenThrow(new RecursoNaoEncontradoException("Nenhuma estratégia vigente"));

        mockMvc.perform(get("/api/estrategias/vigente").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorIdRetorna200() throws Exception {
        when(estrategiaService.buscarPorId("id-1")).thenReturn(estrategiaExemplo());

        mockMvc.perform(get("/api/estrategias/id-1").with(user("operador-1").roles("OPERADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarRetorna200() throws Exception {
        when(estrategiaService.atualizar(eq("id-1"), any())).thenReturn(estrategiaExemplo());

        mockMvc.perform(put("/api/estrategias/id-1")
                        .with(user("lider-1").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isOk());
    }

    @Test
    void encerrarRetorna200() throws Exception {
        when(estrategiaService.encerrar("id-1")).thenReturn(estrategiaExemplo());

        mockMvc.perform(patch("/api/estrategias/id-1/encerrar").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/api/estrategias/id-1").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isNoContent());
    }
}
