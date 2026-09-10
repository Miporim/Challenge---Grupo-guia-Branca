package br.com.fiap.aguiabranca.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.CorrelationIdFilter;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import br.com.fiap.aguiabranca.shared.CredenciaisInvalidasException;
import br.com.fiap.aguiabranca.shared.EmailJaCadastradoException;
import br.com.fiap.aguiabranca.shared.RateLimitExcedidoException;
import br.com.fiap.aguiabranca.usuario.dto.LoginRequest;
import br.com.fiap.aguiabranca.usuario.dto.LoginResponse;
import br.com.fiap.aguiabranca.usuario.dto.RegistroRequest;
import br.com.fiap.aguiabranca.usuario.dto.UsuarioResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Contrato HTTP de /api/auth — critério de aceitação da Etapa A. A
 * verificação de role (matriz de acesso) fica em
 * {@link UsuarioControllerAccessTest}, contra o service real.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CorrelationIdFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // DECISION: ObjectMapper local (não o bean do contexto) — o slice de
    // @WebMvcTest usado aqui não carrega o autoconfig completo do Jackson;
    // só precisamos serializar DTOs simples para o corpo da requisição.
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AuthService authService;

    @Test
    void registrarComSucessoRetorna201() throws Exception {
        var request = new RegistroRequest("Ana Operadora", "ana@aguiabranca.com", "senha1234");
        var resposta = new UsuarioResponse("id-1", "Ana Operadora", "ana@aguiabranca.com",
                Role.OPERADOR, true, Instant.now());
        when(authService.registrar(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@aguiabranca.com"))
                .andExpect(jsonPath("$.role").value("OPERADOR"));
    }

    @Test
    void registrarComEmailDuplicadoRetorna409() throws Exception {
        var request = new RegistroRequest("Ana", "ana@aguiabranca.com", "senha1234");
        when(authService.registrar(any())).thenThrow(new EmailJaCadastradoException("ana@aguiabranca.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void registrarComCorpoInvalidoRetorna400() throws Exception {
        var request = new RegistroRequest("", "nao-e-email", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComSucessoRetornaToken() throws Exception {
        var request = new LoginRequest("ana@aguiabranca.com", "senha1234");
        var usuarioResp = new UsuarioResponse("id-1", "Ana", "ana@aguiabranca.com", Role.OPERADOR, true, Instant.now());
        var resposta = new LoginResponse("token-fake", "Bearer", Instant.now().plusSeconds(28800), usuarioResp);
        when(authService.login(any())).thenReturn(resposta);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-fake"));
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401ComMensagemPadrao() throws Exception {
        var request = new LoginRequest("inexistente@aguiabranca.com", "errada123");
        when(authService.login(any())).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"));
    }

    @Test
    void loginComMuitasTentativasRetorna429() throws Exception {
        var request = new LoginRequest("ana@aguiabranca.com", "errada123");
        when(authService.login(any())).thenThrow(new RateLimitExcedidoException("Muitas tentativas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void meSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meComTokenValidoRetorna200() throws Exception {
        var resposta = new UsuarioResponse("id-1", "Ana", "ana@aguiabranca.com", Role.OPERADOR, true, Instant.now());
        when(authService.me()).thenReturn(resposta);

        mockMvc.perform(get("/api/auth/me").with(user("id-1").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id-1"));
    }
}
