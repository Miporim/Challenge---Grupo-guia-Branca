package br.com.fiap.aguiabranca.shared;

/** Mapeada para 409 pelo {@link GlobalExceptionHandler}. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
