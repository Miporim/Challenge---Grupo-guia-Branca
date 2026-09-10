package br.com.fiap.aguiabranca.ia;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Cache de 1h para {@code /api/ia/insights} (seção 7.3) — "para não
 * quemar la cuota em cada abertura de tela". Em memória, mesmo padrão
 * simples do {@code LoginRateLimiter} (Etapa A): não há um segundo
 * provedor de cache na dependência para uma única chave.
 */
@Component
public class InsightsCache {

    private static final Duration TTL = Duration.ofHours(1);
    private static final String CHAVE_UNICA = "insights";

    private final ConcurrentHashMap<String, Entrada> cache = new ConcurrentHashMap<>();

    public String obter() {
        Entrada entrada = cache.get(CHAVE_UNICA);
        if (entrada == null || entrada.expirouEm.isBefore(Instant.now())) {
            return null;
        }
        return entrada.valor;
    }

    public void guardar(String valor) {
        cache.put(CHAVE_UNICA, new Entrada(valor, Instant.now().plus(TTL)));
    }

    private record Entrada(String valor, Instant expirouEm) {
    }
}
