package br.com.fiap.aguiabranca.relatorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import br.com.fiap.aguiabranca.estrategia.EstrategiaRepository;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.projeto.ProjetoService;

/**
 * Testa as fórmulas da seção 4 isoladas do Mongo (métodos de pacote,
 * não expostos publicamente — só para este teste). A execução real do
 * pipeline de agregação não tem cobertura automatizada nesta etapa: não
 * há Mongo embebido/Testcontainers ainda (isso é Etapa H) e os testes
 * não podem apontar para o Atlas (seção 8).
 */
class RelatorioServiceFormulaTest {

    private final RelatorioService relatorioService = new RelatorioService(
            mock(MongoTemplate.class),
            mock(EstrategiaRepository.class),
            mock(IdeiaRepository.class),
            mock(ProjetoService.class));

    @Test
    void roiComInvestimentoPositivo() {
        // lucro 500, investimento 1000 -> 50.00%
        BigDecimal roi = relatorioService.calcularRoi(new BigDecimal("500"), new BigDecimal("1000"));
        assertEquals(new BigDecimal("50.00"), roi);
    }

    @Test
    void roiComInvestimentoZeroDevolveZero() {
        BigDecimal roi = relatorioService.calcularRoi(new BigDecimal("500"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), roi);
    }

    @Test
    void roiComLucroNegativo() {
        BigDecimal roi = relatorioService.calcularRoi(new BigDecimal("-200"), new BigDecimal("1000"));
        assertEquals(new BigDecimal("-20.00"), roi);
    }

    @Test
    void arredondaComHalfUp() {
        BigDecimal valor = relatorioService.arredondar(new BigDecimal("10.005"));
        assertEquals(new BigDecimal("10.01"), valor);
    }

    @Test
    void arredondaNuloComoZero() {
        assertEquals(new BigDecimal("0.00"), relatorioService.arredondar(null));
    }
}
