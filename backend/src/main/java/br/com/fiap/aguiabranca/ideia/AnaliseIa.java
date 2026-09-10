package br.com.fiap.aguiabranca.ideia;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embebido em {@link Ideia#getAnaliseIa()}, nullable — populado pela
 * Etapa G (pacote {@code ia}). Estrutura já definida na seção 3/7.1 para
 * não precisar migrar o documento depois.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseIa {
    private int score;
    private int aderenciaEstrategia;
    private String justificativa;
    private List<String> riscos;
    private String modelo;
    private Instant geradoEm;
}
