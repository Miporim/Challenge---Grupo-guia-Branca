package br.com.fiap.aguiabranca.ia;

import java.time.Duration;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Groq e Ollama expõem a mesma API {@code /chat/completions} (compatível
 * com OpenAI) — um cliente serve os dois perfis alternativos da seção 7,
 * só troca {@code baseUrl}/{@code modelo}/{@code apiKey} (ver
 * {@link IaClientConfig}). Ollama local geralmente não exige key — nesse
 * caso {@code apiKey} vem vazio e o header {@code Authorization} não é
 * enviado.
 */
public class OpenAiCompatibleClient implements IaClient {

    private final String baseUrl;
    private final String modelo;
    private final String apiKey;
    private final RestClient restClient;

    public OpenAiCompatibleClient(String baseUrl, String modelo, String apiKey) {
        this.baseUrl = baseUrl;
        this.modelo = modelo;
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String nomeModelo() {
        return modelo;
    }

    @Override
    public String gerarTexto(String prompt) {
        return chamarComRetentativa(prompt);
    }

    private String chamarComRetentativa(String prompt) {
        RuntimeException ultimaFalha = null;
        for (int tentativa = 0; tentativa < 2; tentativa++) {
            try {
                return chamar(prompt);
            } catch (RestClientException ex) {
                ultimaFalha = ex;
            }
        }
        throw new IaIndisponivelException("Provedor de IA indisponível após retentativa", ultimaFalha);
    }

    private String chamar(String prompt) {
        ChatRequest corpo = new ChatRequest(modelo, List.of(new Mensagem("user", prompt)));

        var requestSpec = restClient.post()
                .uri(baseUrl + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            requestSpec = requestSpec.header("Authorization", "Bearer " + apiKey);
        }

        ChatResponse resposta = requestSpec.body(corpo).retrieve().body(ChatResponse.class);

        if (resposta == null || resposta.choices() == null || resposta.choices().isEmpty()) {
            throw new IaIndisponivelException("Provedor não retornou nenhuma escolha");
        }
        return resposta.choices().get(0).message().content();
    }

    private record ChatRequest(String model, List<Mensagem> messages) {
    }

    private record Mensagem(String role, String content) {
    }

    private record ChatResponse(List<Escolha> choices) {
    }

    private record Escolha(Mensagem message) {
    }
}
