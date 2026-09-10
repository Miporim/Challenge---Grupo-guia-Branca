package br.com.fiap.aguiabranca.ia;

/**
 * Mapeada para 502 — a resposta do provedor não é um JSON válido ou não
 * bate com o schema esperado (seção 7.1). Nunca persistir uma resposta
 * a meio parsear: o service descarta tudo e lança isto antes de salvar.
 */
public class IaRespostaInvalidaException extends RuntimeException {
    public IaRespostaInvalidaException(String mensagem) {
        super(mensagem);
    }

    public IaRespostaInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
