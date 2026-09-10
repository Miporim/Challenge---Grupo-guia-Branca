package br.com.fiap.aguiabranca.usuario;

/**
 * Perfis de acesso da plataforma. A hierarquia de permissões está descrita
 * na especificação (seção 4) e é aplicada via {@code @PreAuthorize} nos services.
 */
public enum Role {
    OPERADOR,
    GESTOR,
    LIDER
}
