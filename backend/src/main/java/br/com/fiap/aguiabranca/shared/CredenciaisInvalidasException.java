package br.com.fiap.aguiabranca.shared;

/**
 * Mapeada para 401. Mensagem sempre {@code "Credenciais inválidas"} — não
 * pode revelar se o e-mail existe, se está inativo, ou se foi a senha.
 */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
