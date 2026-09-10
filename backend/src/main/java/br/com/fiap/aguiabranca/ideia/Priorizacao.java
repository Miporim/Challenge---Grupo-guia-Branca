package br.com.fiap.aguiabranca.ideia;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Embebido em {@link Ideia#getPriorizacoes()} — um voto por gestor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Priorizacao {
    private String gestorId;
    private int nota;
    private String comentario;
    private Instant em;
}
