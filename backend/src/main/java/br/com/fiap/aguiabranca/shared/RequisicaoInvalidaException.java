package br.com.fiap.aguiabranca.shared;

/** Mapeada para 400 — regra de negócio de entrada inválida (fora do Bean Validation). */
public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
