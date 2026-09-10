package br.com.fiap.aguiabranca.ideia;

import java.math.BigDecimal;
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

@Document(collection = "ideias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ideia {

    @Id
    private String id;

    private String titulo;
    private String descricao;

    @Indexed
    private String autorId;

    @Indexed
    private String estrategiaId;

    @Indexed
    private StatusIdeia status;

    private Nivel impacto;
    private Nivel esforco;

    @Builder.Default
    private List<Priorizacao> priorizacoes = new ArrayList<>();

    @Indexed(direction = IndexDirection.DESCENDING)
    private BigDecimal notaMedia;

    private int totalVotos;

    private AnaliseIa analiseIa;

    private String projetoId;

    private Instant criadoEm;
    private Instant atualizadoEm;
}
