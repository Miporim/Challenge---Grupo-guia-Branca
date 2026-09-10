package br.com.fiap.aguiabranca.shared;

/** Mapeada para 404 pelo {@link GlobalExceptionHandler}. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
