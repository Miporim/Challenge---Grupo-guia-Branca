package br.com.fiap.aguiabranca.projeto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "projetos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {

    @Id
    private String id;

    private String titulo;
    private String descricao;

    @Indexed
    private String estrategiaId;

    private String ideiaOrigemId;

    @Indexed
    private String gestorId;

    @Indexed
    private EtapaProjeto etapa;

    @Indexed
    private StatusProjeto status;

    private BigDecimal investimento;

    private Instant prazoInicio;
    private Instant prazoFim;
    private Instant concluidoEm;

    private int percentualConcluido;

    @Builder.Default
    private List<Resultado> resultados = new ArrayList<>();

    private Instant criadoEm;
    private Instant atualizadoEm;
}
