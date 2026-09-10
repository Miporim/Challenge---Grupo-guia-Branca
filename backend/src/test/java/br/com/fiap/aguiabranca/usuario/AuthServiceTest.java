package br.com.fiap.aguiabranca.usuario;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.fiap.aguiabranca.auditoria.Acao;
import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.auditoria.Ator;
import br.com.fiap.aguiabranca.security.JwtService;
import br.com.fiap.aguiabranca.security.LoginRateLimiter;
import br.com.fiap.aguiabranca.shared.CredenciaisInvalidasException;
import br.com.fiap.aguiabranca.usuario.dto.LoginRequest;

/** Regras de negócio puras — a matriz de acesso não se aplica aqui (endpoints públicos). */
class AuthServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final LoginRateLimiter loginRateLimiter = mock(LoginRateLimiter.class);
    private final UsuarioService usuarioService = mock(UsuarioService.class);
    private final AuditoriaPublisher auditoriaPublisher = mock(AuditoriaPublisher.class);
    private final AuthService authService = new AuthService(
            usuarioRepository, passwordEncoder, jwtService, loginRateLimiter, usuarioService, auditoriaPublisher);

    @Test
    void loginComSucessoAuditaLogin() {
        Usuario usuario = Usuario.builder().id("usr-1").email("ana@aguiabranca.com").role(Role.OPERADOR).ativo(true)
                .senhaHash("hash").build();
        when(usuarioRepository.findByEmail("ana@aguiabranca.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", "hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn(
                new JwtService.TokenGerado("token-fake", java.time.Instant.now().plusSeconds(28800)));

        var resposta = authService.login(new LoginRequest("ana@aguiabranca.com", "senha1234"));

        assertNotNull(resposta);
        verify(auditoriaPublisher).publicarComAtor(
                org.mockito.ArgumentMatchers.argThat((Ator a) -> "usr-1".equals(a.getUserId())),
                eq(Acao.LOGIN), eq("usuario"), eq("usr-1"), any(), any());
        verify(loginRateLimiter).limpar("ana@aguiabranca.com");
    }

    @Test
    void loginComSenhaErradaAuditaLoginFalha() {
        Usuario usuario = Usuario.builder().id("usr-1").email("ana@aguiabranca.com").role(Role.OPERADOR).ativo(true)
                .senhaHash("hash").build();
        when(usuarioRepository.findByEmail("ana@aguiabranca.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("ana@aguiabranca.com", "errada")));

        verify(auditoriaPublisher).publicarComAtor(
                org.mockito.ArgumentMatchers.argThat((Ator a) -> "usr-1".equals(a.getUserId())),
                eq(Acao.LOGIN_FALHA), eq("usuario"), eq("usr-1"), any(), any());
        verify(loginRateLimiter).registrarFalha("ana@aguiabranca.com");
    }

    @Test
    void loginComEmailInexistenteAuditaLoginFalhaSemUserId() {
        when(usuarioRepository.findByEmail("fantasma@aguiabranca.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> authService.login(new LoginRequest("fantasma@aguiabranca.com", "qualquer")));

        verify(auditoriaPublisher).publicarComAtor(
                org.mockito.ArgumentMatchers.argThat((Ator a) -> a.getUserId() == null
                        && "fantasma@aguiabranca.com".equals(a.getEmail())),
                eq(Acao.LOGIN_FALHA), eq("usuario"), eq(null), any(), any());
    }

    @Test
    void loginComRateLimitExcedidoNaoConsultaRepositorio() {
        when(loginRateLimiter.excedeuLimite("ana@aguiabranca.com")).thenReturn(true);

        assertThrows(br.com.fiap.aguiabranca.shared.RateLimitExcedidoException.class,
                () -> authService.login(new LoginRequest("ana@aguiabranca.com", "senha1234")));

        verify(usuarioRepository, times(0)).findByEmail(any());
    }
}
