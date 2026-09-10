package br.com.fiap.aguiabranca.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class InsightsCacheTest {

    private final InsightsCache cache = new InsightsCache();

    @Test
    void semNadaGuardadoDevolveNulo() {
        assertNull(cache.obter());
    }

    @Test
    void devolveOValorGuardado() {
        cache.guardar("{\"leituraGeral\":\"ok\"}");

        assertEquals("{\"leituraGeral\":\"ok\"}", cache.obter());
    }
}
