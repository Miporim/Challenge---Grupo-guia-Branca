package br.com.fiap.aguiabranca.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.aguiabranca.config.SecurityConfig;
import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import br.com.fiap.aguiabranca.security.JwtService;
import br.com.fiap.aguiabranca.usuario.dto.CriarUsuarioRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Contrato HTTP de /api/usuarios: mapeamento de request/response e códigos
 * de status. {@code UsuarioService} é mockado aqui — a autorização por
 * role (o {@code @PreAuthorize} do service) tem sua própria prova em
 * {@link UsuarioServiceMethodSecurityTest}, contra o service real.
 */
@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, JwtService.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private UsuarioService usuarioService;

    private CriarUsuarioRequest requestValido() {
        return new CriarUsuarioRequest("Novo Gestor", "novo.gestor@aguiabranca.com", "senha1234", Role.GESTOR);
    }

    @Test
    void criarComSucessoRetorna201() throws Exception {
        Usuario criado = Usuario.builder()
                .id("id-novo")
                .nome("Novo Gestor")
                .email("novo.gestor@aguiabranca.com")
                .role(Role.GESTOR)
                .ativo(true)
                .criadoEm(Instant.now())
                .build();
        when(usuarioService.criarGestorOuLider(any())).thenReturn(criado);

        mockMvc.perform(post("/api/usuarios")
                        .with(user("lider-1").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated());
    }

    @Test
    void criarSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarComSucessoRetorna200() throws Exception {
        when(usuarioService.listar(any(), any(PageRequest.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/usuarios").with(user("lider-1").roles("LIDER")))
                .andExpect(status().isOk());
    }

    @Test
    void alterarAtivoComSucessoRetorna200() throws Exception {
        Usuario usuario = Usuario.builder()
                .id("id-1")
                .nome("Gestor")
                .email("gestor@aguiabranca.com")
                .role(Role.GESTOR)
                .ativo(false)
                .criadoEm(Instant.now())
                .build();
        when(usuarioService.alterarAtivo("id-1", false)).thenReturn(usuario);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/usuarios/id-1/ativo")
                        .with(user("lider-1").roles("LIDER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": false}"))
                .andExpect(status().isOk());
    }
}
