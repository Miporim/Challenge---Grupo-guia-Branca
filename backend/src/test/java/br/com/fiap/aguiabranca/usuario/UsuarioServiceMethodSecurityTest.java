package br.com.fiap.aguiabranca.usuario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.usuario.dto.CriarUsuarioRequest;
import org.springframework.data.domain.PageRequest;

/**
 * Testa o {@code @PreAuthorize} de {@link UsuarioService} diretamente — sem
 * subir a camada web nem autoconfig do Boot (por isso um
 * {@code @ContextConfiguration} mínimo, não {@code @WebMvcTest} nem
 * {@code @SpringBootTest}: nenhum dos dois é necessário aqui e ambos
 * tentam preparar infraestrutura do Mongo por causa do
 * {@code auto-index-creation}). Esta é a prova da "matriz de acesso" —
 * critério de aceitação da Etapa A.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        UsuarioServiceMethodSecurityTest.SecurityTestConfig.class,
        UsuarioService.class
})
class UsuarioServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    private CriarUsuarioRequest requestValido() {
        return new CriarUsuarioRequest("Novo Gestor", "novo.gestor@aguiabranca.com", "senha1234", Role.GESTOR);
    }

    @Test
    @WithMockUser(roles = "LIDER")
    void liderConsegueCriarGestor() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        assertDoesNotThrow(() -> usuarioService.criarGestorOuLider(requestValido()));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueCriarUsuario() {
        assertThrows(AccessDeniedException.class,
                () -> usuarioService.criarGestorOuLider(requestValido()));
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    void gestorNaoConsegueCriarUsuario() {
        assertThrows(AccessDeniedException.class,
                () -> usuarioService.criarGestorOuLider(requestValido()));
    }

    @Test
    void semAutenticacaoNaoConsegueCriarUsuario() {
        // Sem Authentication alguma no contexto (nem anônima — este teste não
        // sobe a camada web), o security interceptor rejeita antes mesmo de
        // avaliar o hasRole, com AuthenticationCredentialsNotFoundException
        // em vez de AccessDeniedException. Na cadeia HTTP real, isso é o
        // caminho que o JwtAuthenticationEntryPoint traduz para 401.
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> usuarioService.criarGestorOuLider(requestValido()));
    }

    @Test
    @WithMockUser(roles = "LIDER")
    void liderConsegueListarUsuarios() {
        when(usuarioRepository.findAll(any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        assertDoesNotThrow(() -> usuarioService.listar(null, PageRequest.of(0, 20)));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueListarUsuarios() {
        assertThrows(AccessDeniedException.class,
                () -> usuarioService.listar(null, PageRequest.of(0, 20)));
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    void gestorNaoConsegueAlterarAtivo() {
        assertThrows(AccessDeniedException.class,
                () -> usuarioService.alterarAtivo("qualquer-id", false));
    }

    @Test
    @WithMockUser(roles = "LIDER")
    void liderConsegueAlterarAtivo() {
        Usuario usuario = Usuario.builder().id("id-1").role(Role.OPERADOR).ativo(true).build();
        when(usuarioRepository.findById("id-1")).thenReturn(java.util.Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> usuarioService.alterarAtivo("id-1", false));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueAlterarAtivo() {
        assertThrows(AccessDeniedException.class,
                () -> usuarioService.alterarAtivo("qualquer-id", false));
    }
}
