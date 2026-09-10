package br.com.fiap.aguiabranca.auditoria;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEvento {

    @Id
    private String id;

    private Ator ator;
    private Acao acao;

    @Indexed
    private String recurso;
    private String recursoId;

    private Alteracoes alteracoes;

    private String ip;
    private String correlationId;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant em;
}
