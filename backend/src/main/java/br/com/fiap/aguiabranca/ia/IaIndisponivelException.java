package br.com.fiap.aguiabranca.ia;

/**
 * Mapeada para 503 — sem {@code GEMINI_API_KEY} configurada, timeout
 * após a 1 retentativa, ou provedor fora do ar (seção 7). Nenhum
 * usuário fica bloqueado por não ter a key: a aplicação sobe normal, só
 * este endpoint específico degrada.
 */
public class IaIndisponivelException extends RuntimeException {
    public IaIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
