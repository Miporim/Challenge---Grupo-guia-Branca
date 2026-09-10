package br.com.fiap.aguiabranca.projeto.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarProjetoRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Estratégia é obrigatória")
        String estrategiaId,

        // Nullable — só quando o projeto nasce de uma ideia aprovada.
        String ideiaOrigemId,

        @NotNull(message = "Investimento é obrigatório")
        @DecimalMin(value = "0", message = "Investimento não pode ser negativo")
        BigDecimal investimento,

        @NotNull(message = "Prazo início é obrigatório")
        Instant prazoInicio,

        @NotNull(message = "Prazo fim é obrigatório")
        Instant prazoFim,

        // DECISION: opcionais — se omitidos, PLANEJAMENTO/NO_PRAZO. Não
        // especificado default na seção 3.
        EtapaProjeto etapa,
        StatusProjeto status
) {
}
