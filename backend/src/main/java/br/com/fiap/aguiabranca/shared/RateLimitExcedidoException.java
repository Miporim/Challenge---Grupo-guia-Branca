package br.com.fiap.aguiabranca.shared;

/** Mapeada para 429 pelo {@link GlobalExceptionHandler}. */
public class RateLimitExcedidoException extends RuntimeException {
    public RateLimitExcedidoException(String mensagem) {
        super(mensagem);
    }
}
