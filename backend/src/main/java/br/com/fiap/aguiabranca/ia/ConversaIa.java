package br.com.fiap.aguiabranca.ia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "ia_conversas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversaIa {

    @Id
    private String id;

    @Indexed
    private String gestorId;

    private String titulo;

    @Builder.Default
    private List<MensagemIa> mensagens = new ArrayList<>();

    private Instant criadoEm;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant atualizadoEm;
}
