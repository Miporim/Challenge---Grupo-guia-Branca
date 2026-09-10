package br.com.fiap.aguiabranca.ia;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteRequest;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteResponse;
import br.com.fiap.aguiabranca.ia.dto.ChatRequest;
import br.com.fiap.aguiabranca.ia.dto.ConversaResponse;
import br.com.fiap.aguiabranca.ia.dto.InsightsResponse;
import br.com.fiap.aguiabranca.ideia.AnaliseIa;
import br.com.fiap.aguiabranca.security.CorrelationIdFilter;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contrato HTTP de /api/ia — {@code IaService} mockado, incluindo o
 * mapeamento de 502/503 pelo {@link IaExceptionHandler}. A matriz de
 * acesso tem sua prova em {@link IaServiceMethodSecurityTest}.
 */
@WebMvcTest(IaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class, IaExceptionHandler.class})
class IaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IaService iaService;

    @Test
    void analisarIdeiaComSucessoRetorna200() throws Exception {
        AnaliseIa analise = AnaliseIa.builder().score(80).aderenciaEstrategia(60).justificativa("ok")
                .riscos(List.of("a")).modelo("gemini-2.5-flash-lite").geradoEm(Instant.now()).build();
        when(iaService.analisarIdeia("id-1")).thenReturn(analise);

        mockMvc.perform(post("/api/ia/ideias/id-1/analise").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void analisarIdeiaSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/ia/ideias/id-1/analise"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analisarIdeiaComIaIndisponivelRetorna503() throws Exception {
        when(iaService.analisarIdeia("id-1")).thenThrow(new IaIndisponivelException("GEMINI_API_KEY não configurada"));

        mockMvc.perform(post("/api/ia/ideias/id-1/analise").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void analisarIdeiaComRespostaInvalidaRetorna502() throws Exception {
        when(iaService.analisarIdeia("id-1")).thenThrow(new IaRespostaInvalidaException("schema inválido"));

        mockMvc.perform(post("/api/ia/ideias/id-1/analise").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isBadGateway());
    }

    @Test
    void analisarLoteRetorna200() throws Exception {
        when(iaService.analisarLote("estrategia-1")).thenReturn(new AnaliseLoteResponse(3, 1));

        mockMvc.perform(post("/api/ia/ideias/analise-lote")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnaliseLoteRequest("estrategia-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void chatRetorna200() throws Exception {
        when(iaService.chat(any())).thenReturn(new ConversaResponse("conversa-1", "titulo", List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/ia/chat")
                        .with(user("gestor-1").roles("GESTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChatRequest(null, "oi"))))
                .andExpect(status().isOk());
    }

    @Test
    void listarConversasRetorna200() throws Exception {
        when(iaService.listarConversas(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/ia/chat/conversas").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void buscarConversaRetorna200() throws Exception {
        when(iaService.buscarConversa("conversa-1")).thenReturn(
                new ConversaResponse("conversa-1", "titulo", List.of(), Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/ia/chat/conversas/conversa-1").with(user("gestor-1").roles("GESTOR")))
                .andExpect(status().isOk());
    }

    @Test
    void insightsRetorna200() throws Exception {
        when(iaService.insights()).thenReturn(new InsightsResponse("ok", List.of("a"), List.of("b")));

        mockMvc.perform(post("/api/ia/insights").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }
}
