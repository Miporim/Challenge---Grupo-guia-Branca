package br.com.fiap.aguiabranca.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginRateLimiterTest {

    private final LoginRateLimiter rateLimiter = new LoginRateLimiter();

    @Test
    void naoExcedeLimiteAntesDeCincoFalhas() {
        String email = "operador@aguiabranca.com";

        for (int i = 0; i < 4; i++) {
            rateLimiter.registrarFalha(email);
        }

        assertFalse(rateLimiter.excedeuLimite(email));
    }

    @Test
    void excedeLimiteNaQuintaFalha() {
        String email = "gestor@aguiabranca.com";

        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha(email);
        }

        assertTrue(rateLimiter.excedeuLimite(email));
    }

    @Test
    void limparZeraContagem() {
        String email = "lider@aguiabranca.com";

        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFalha(email);
        }
        rateLimiter.limpar(email);

        assertFalse(rateLimiter.excedeuLimite(email));
    }

    @Test
    void emailEhTratadoSemDiferencaDeCaixa() {
        rateLimiter.registrarFalha("Usuario@AguiaBranca.com");
        rateLimiter.registrarFalha("Usuario@AguiaBranca.com");
        rateLimiter.registrarFalha("Usuario@AguiaBranca.com");
        rateLimiter.registrarFalha("Usuario@AguiaBranca.com");
        rateLimiter.registrarFalha("usuario@aguiabranca.com");

        assertTrue(rateLimiter.excedeuLimite("USUARIO@AGUIABRANCA.COM"));
    }
}
