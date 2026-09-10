package br.com.fiap.aguiabranca.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/**
 * Rate limit de login: 5 tentativas falhadas por e-mail em 15 minutos → 429.
 * Implementação em memória com {@code ConcurrentHashMap}, suficiente para o
 * escopo do projeto (seção 5).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, List<Instant>> tentativasFalhasPorEmail = new ConcurrentHashMap<>();

    public boolean excedeuLimite(String email) {
        return tentativasRecentes(email).size() >= MAX_TENTATIVAS;
    }

    public void registrarFalha(String email) {
        List<Instant> tentativas = tentativasFalhasPorEmail.computeIfAbsent(
                normalizar(email), e -> new CopyOnWriteArrayList<>());
        tentativas.add(Instant.now());
    }

    public void limpar(String email) {
        tentativasFalhasPorEmail.remove(normalizar(email));
    }

    private List<Instant> tentativasRecentes(String email) {
        List<Instant> tentativas = tentativasFalhasPorEmail.get(normalizar(email));
        if (tentativas == null) {
            return List.of();
        }
        Instant limite = Instant.now().minus(JANELA);
        tentativas.removeIf(instante -> instante.isBefore(limite));
        return tentativas;
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
