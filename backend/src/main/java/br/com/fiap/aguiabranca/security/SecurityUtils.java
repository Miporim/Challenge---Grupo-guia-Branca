package br.com.fiap.aguiabranca.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.aguiabranca.usuario.Role;

/**
 * Acesso centralizado ao usuário autenticado, para não repetir leitura do
 * {@code SecurityContextHolder} em cada service — ver seção 5.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** @return o id do usuário autenticado (claim {@code sub}), ou {@code null} se anônimo. */
    public static String getUsuarioId() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            return null;
        }
        return String.valueOf(autenticacao.getPrincipal());
    }

    /** @return o role do usuário autenticado, ou {@code null} se anônimo ou sem authority reconhecida. */
    public static Role getRole() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null) {
            return null;
        }
        for (GrantedAuthority authority : autenticacao.getAuthorities()) {
            String nome = authority.getAuthority();
            if (nome.startsWith("ROLE_")) {
                return Role.valueOf(nome.substring("ROLE_".length()));
            }
        }
        return null;
    }

    /**
     * @return o e-mail do usuário autenticado (guardado em
     * {@code details} pelo {@code JwtAuthenticationFilter}), ou
     * {@code null} se anônimo.
     */
    public static String getEmail() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || autenticacao.getDetails() == null) {
            return null;
        }
        return String.valueOf(autenticacao.getDetails());
    }
}
