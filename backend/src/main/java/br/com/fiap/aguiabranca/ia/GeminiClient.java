package br.com.fiap.aguiabranca.ia;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Provedor principal (seção 7) — API REST do Gemini via {@code RestClient}
 * puro. DECISION: Spring AI 2.0.x não foi usado — a especificação
 * explicitamente permite pular para isto se der problema com o Boot
 * 4.1.1, e verificar a versão exata no Maven Central e validar a
 * autoconfiguração de um framework grande contra um Boot tão recente
 * não é viável neste ambiente sem acesso à API real para testar de
 * qualquer forma. Documentado também no README.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "app.ia.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiClient implements IaClient {

    private final String apiKey;
    private final String baseUrl;
    private final String modelo;
    private final RestClient restClient;

    public GeminiClient(
            @Value("${app.ia.gemini.api-key:}") String apiKey,
            @Value("${app.ia.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ia.gemini.model:gemini-2.5-flash-lite}") String modelo) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelo = modelo;

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
        if (apiKey == null || apiKey.isBlank()) {
            throw new IaIndisponivelException(
                    "GEMINI_API_KEY não configurada — funcionalidades de IA desabilitadas");
        }
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
        throw new IaIndisponivelException("Gemini indisponível após retentativa", ultimaFalha);
    }

    private String chamar(String prompt) {
        GerarConteudoRequest corpo = new GerarConteudoRequest(
                List.of(new Conteudo(List.of(new Parte(prompt)))));

        GerarConteudoResponse resposta = restClient.post()
                .uri(baseUrl + "/models/{modelo}:generateContent?key={key}", modelo, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .body(GerarConteudoResponse.class);

        if (resposta == null || resposta.candidates() == null || resposta.candidates().isEmpty()) {
            throw new IaIndisponivelException("Gemini não retornou candidatos");
        }
        Conteudo conteudo = resposta.candidates().get(0).content();
        if (conteudo == null || conteudo.parts() == null || conteudo.parts().isEmpty()) {
            throw new IaIndisponivelException("Gemini retornou resposta vazia");
        }
        return conteudo.parts().get(0).text();
    }

    private record GerarConteudoRequest(List<Conteudo> contents) {
    }

    private record Conteudo(List<Parte> parts) {
    }

    private record Parte(String text) {
    }

    private record GerarConteudoResponse(List<Candidato> candidates) {
    }

    private record Candidato(Conteudo content) {
    }
}
