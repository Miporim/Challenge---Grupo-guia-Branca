package br.com.fiap.aguiabranca.estrategia;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "estrategias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estrategia {

    @Id
    private String id;

    private String titulo;
    private String descricao;

    @Indexed
    private Categoria categoria;

    @Indexed
    private String campanha;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant vigenciaInicio;

    private Instant vigenciaFim;

    @Indexed
    private EstrategiaStatus status;

    private String criadoPor;

    private Instant criadoEm;
    private Instant atualizadoEm;
}
