package br.com.fiap.aguiabranca.ia;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Embebido em {@link ConversaIa#getMensagens()}. {@code papel}: {@code user} | {@code assistant}. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensagemIa {
    private String papel;
    private String texto;
    private Instant em;
}
