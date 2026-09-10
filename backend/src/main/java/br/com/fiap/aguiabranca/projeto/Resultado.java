package br.com.fiap.aguiabranca.projeto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Embebido em {@link Projeto#getResultados()} — histórico, nunca sobrescrito. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resultado {
    private Instant data;
    private BigDecimal receita;
    private BigDecimal economia;
    private BigDecimal ganhoProdutividadePct;
    private String observacao;
    private String registradoPor;
}
