package br.com.fiap.aguiabranca.ia;

/**
 * Abstração sobre o provedor de IA — {@code IaService} não conhece
 * Gemini/Groq/Ollama, só isto (seção 7.4). Troca de provedor é só
 * {@code application.yaml} (propriedade {@code app.ia.provider}).
 */
public interface IaClient {

    /**
     * @throws IaIndisponivelException sem key configurada, timeout
     * (após 1 retentativa) ou provedor fora do ar.
     */
    String gerarTexto(String prompt);

    /** Nome do modelo usado — vai no campo {@code modelo} de {@code AnaliseIa}. */
    String nomeModelo();
}
