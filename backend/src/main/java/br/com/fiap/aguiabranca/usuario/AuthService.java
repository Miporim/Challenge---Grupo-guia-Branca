package br.com.fiap.aguiabranca.usuario;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.auditoria.Acao;
import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.auditoria.Ator;
import br.com.fiap.aguiabranca.security.JwtService;
import br.com.fiap.aguiabranca.security.LoginRateLimiter;
import br.com.fiap.aguiabranca.security.SecurityUtils;
import br.com.fiap.aguiabranca.shared.CredenciaisInvalidasException;
import br.com.fiap.aguiabranca.shared.RateLimitExcedidoException;
import br.com.fiap.aguiabranca.usuario.dto.LoginRequest;
import br.com.fiap.aguiabranca.usuario.dto.LoginResponse;
import br.com.fiap.aguiabranca.usuario.dto.RegistroRequest;
import br.com.fiap.aguiabranca.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;

/**
 * Registro, login e {@code /me}. O rate limit e a mensagem idêntica para
 * e-mail inexistente x senha incorreta estão aqui — ver seção 4 e 5.
 *
 * Audita LOGIN/LOGIN_FALHA diretamente com o e-mail/usuário que tem em
 * mãos — não usa {@code SecurityUtils} porque no momento do login ainda
 * não há usuário autenticado no contexto.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final UsuarioService usuarioService;
    private final AuditoriaPublisher auditoriaPublisher;

    public UsuarioResponse registrar(RegistroRequest request) {
        Usuario usuario = usuarioService.criarOperador(request);
        return UsuarioResponse.de(usuario);
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        if (loginRateLimiter.excedeuLimite(email)) {
            throw new RateLimitExcedidoException(
                    "Muitas tentativas de login para este e-mail. Tente novamente em alguns minutos.");
        }

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(email);

        // DECISION: usuário inativo é rejeitado com a mesma mensagem de
        // credenciais inválidas, para não revelar o estado da conta — não
        // especificado explicitamente na seção 4.
        boolean credenciaisValidas = usuarioEncontrado.isPresent()
                && usuarioEncontrado.get().isAtivo()
                && passwordEncoder.matches(request.senha(), usuarioEncontrado.get().getSenhaHash());

        if (!credenciaisValidas) {
            loginRateLimiter.registrarFalha(email);
            Ator atorFalha = Ator.builder()
                    .userId(usuarioEncontrado.map(Usuario::getId).orElse(null))
                    .email(email)
                    .role(usuarioEncontrado.map(u -> u.getRole().name()).orElse(null))
                    .build();
            auditoriaPublisher.publicarComAtor(atorFalha, Acao.LOGIN_FALHA, "usuario", atorFalha.getUserId(), null, null);
            throw new CredenciaisInvalidasException();
        }

        loginRateLimiter.limpar(email);

        Usuario usuario = usuarioEncontrado.get();
        JwtService.TokenGerado tokenGerado = jwtService.gerarToken(usuario);

        Ator ator = Ator.builder().userId(usuario.getId()).email(usuario.getEmail()).role(usuario.getRole().name()).build();
        auditoriaPublisher.publicarComAtor(ator, Acao.LOGIN, "usuario", usuario.getId(), null, null);

        return new LoginResponse(
                tokenGerado.token(),
                "Bearer",
                tokenGerado.expiraEm(),
                UsuarioResponse.de(usuario));
    }

    public UsuarioResponse me() {
        String usuarioId = SecurityUtils.getUsuarioId();
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        return UsuarioResponse.de(usuario);
    }
}
