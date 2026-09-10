package br.com.fiap.aguiabranca.ia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Lê os templates de {@code src/main/resources/prompts/*.txt} e
 * substitui os placeholders {@code {{chave}}} — seção 7.4: prompts em
 * arquivo, nunca concatenados no Java. Templates são cacheados em
 * memória após a primeira leitura (arquivos imutáveis em runtime).
 */
@Component
public class PromptLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String carregar(String nomeArquivo, Map<String, String> variaveis) {
        String template = cache.computeIfAbsent(nomeArquivo, this::lerArquivo);
        String resultado = template;
        for (Map.Entry<String, String> variavel : variaveis.entrySet()) {
            resultado = resultado.replace("{{" + variavel.getKey() + "}}", variavel.getValue() == null ? "" : variavel.getValue());
        }
        return resultado;
    }

    private String lerArquivo(String nomeArquivo) {
        String caminho = "/prompts/" + nomeArquivo;
        try (InputStream stream = getClass().getResourceAsStream(caminho)) {
            if (stream == null) {
                throw new IllegalStateException("Prompt não encontrado no classpath: " + caminho);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao ler prompt: " + caminho, ex);
        }
    }
}
