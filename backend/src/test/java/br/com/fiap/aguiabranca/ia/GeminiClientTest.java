package br.com.fiap.aguiabranca.ia;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Critério de aceitação da Etapa G: "sem GEMINI_API_KEY, a app arranca
 * e os endpoints de IA devolvem 503" — aqui a prova de que o client
 * nem tenta a chamada de rede sem key (falha rápido, sem timeout).
 */
class GeminiClientTest {

    @Test
    void semApiKeyLancaIndisponivelSemChamarRede() {
        GeminiClient client = new GeminiClient("", "https://generativelanguage.googleapis.com/v1beta", "gemini-2.5-flash-lite");

        assertThrows(IaIndisponivelException.class, () -> client.gerarTexto("qualquer prompt"));
    }

    @Test
    void apiKeyNulaLancaIndisponivel() {
        GeminiClient client = new GeminiClient(null, "https://generativelanguage.googleapis.com/v1beta", "gemini-2.5-flash-lite");

        assertThrows(IaIndisponivelException.class, () -> client.gerarTexto("qualquer prompt"));
    }
}
