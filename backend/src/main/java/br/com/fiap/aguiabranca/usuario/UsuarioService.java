package br.com.fiap.aguiabranca.usuario;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.shared.EmailJaCadastradoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import br.com.fiap.aguiabranca.shared.RequisicaoInvalidaException;
import br.com.fiap.aguiabranca.usuario.dto.CriarUsuarioRequest;
import br.com.fiap.aguiabranca.usuario.dto.RegistroRequest;
import lombok.RequiredArgsConstructor;

/**
 * CRUD e regras de usuário. A autorização fina por role vive aqui
 * ({@code @PreAuthorize}), não nos controllers — ver seção 5.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /** Cadastro público — sempre cria OPERADOR. */
    public Usuario criarOperador(RegistroRequest request) {
        return criar(request.nome(), request.email(), request.senha(), Role.OPERADOR);
    }

    /** {@code POST /api/usuarios} — exclusivo do LIDER, cria GESTOR ou LIDER. */
    @PreAuthorize("hasRole('LIDER')")
    public Usuario criarGestorOuLider(CriarUsuarioRequest request) {
        // DECISION: OPERADOR não pode ser criado por este endpoint — só pelo
        // cadastro público. Não especificado explicitamente na seção 4.
        if (request.role() == Role.OPERADOR) {
            throw new RequisicaoInvalidaException(
                    "Use o cadastro público para criar um OPERADOR");
        }
        return criar(request.nome(), request.email(), request.senha(), request.role());
    }

    @PreAuthorize("hasRole('LIDER')")
    public Page<Usuario> listar(Role roleFiltro, Pageable pageable) {
        if (roleFiltro != null) {
            return usuarioRepository.findByRole(roleFiltro, pageable);
        }
        return usuarioRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('LIDER')")
    public Usuario alterarAtivo(String id, boolean ativo) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(ativo);
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private Usuario criar(String nome, String email, String senha, Role role) {
        String emailNormalizado = normalizarEmail(email);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException(emailNormalizado);
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(emailNormalizado)
                .senhaHash(passwordEncoder.encode(senha))
                .role(role)
                .ativo(true)
                .criadoEm(Instant.now())
                .build();

        return usuarioRepository.save(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
