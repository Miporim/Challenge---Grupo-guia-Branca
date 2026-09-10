package br.com.fiap.aguiabranca.projeto.dto;

import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DECISION: todos os campos opcionais — só atualiza o que vier
 * preenchido. Se {@code etapa} vier {@code CONCLUIDO}, sobrescreve
 * {@code percentualConcluido} para 100 e seta {@code concluidoEm},
 * ignorando o valor de percentual enviado (seção 4).
 */
public record ProgressoRequest(
        EtapaProjeto etapa,
        StatusProjeto status,

        @Min(value = 0, message = "Percentual mínimo é 0")
        @Max(value = 100, message = "Percentual máximo é 100")
        Integer percentualConcluido
) {
}
