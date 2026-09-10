package br.com.fiap.aguiabranca.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans para os perfis alternativos (seção 7) — Groq e Ollama local,
 * ambos via {@link OpenAiCompatibleClient}. {@code app.ia.provider} é a
 * única coisa que precisa mudar no {@code application.yaml} para trocar
 * de provedor; o Gemini (default) está em {@link GeminiClient}.
 */
@Configuration
public class IaClientConfig {

    @Bean
    @ConditionalOnProperty(name = "app.ia.provider", havingValue = "groq")
    public IaClient groqClient(
            @Value("${app.ia.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${app.ia.groq.model:llama-3.3-70b-versatile}") String modelo,
            @Value("${app.ia.groq.api-key:}") String apiKey) {
        return new OpenAiCompatibleClient(baseUrl, modelo, apiKey);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ia.provider", havingValue = "ollama")
    public IaClient ollamaClient(
            @Value("${app.ia.ollama.base-url:http://localhost:11434/v1}") String baseUrl,
            @Value("${app.ia.ollama.model:llama3.2}") String modelo,
            @Value("${app.ia.ollama.api-key:}") String apiKey) {
        return new OpenAiCompatibleClient(baseUrl, modelo, apiKey);
    }
}
