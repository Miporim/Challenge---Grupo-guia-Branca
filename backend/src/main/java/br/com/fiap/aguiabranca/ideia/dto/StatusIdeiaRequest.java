package br.com.fiap.aguiabranca.ideia.dto;

import br.com.fiap.aguiabranca.ideia.StatusIdeia;
import jakarta.validation.constraints.NotNull;

public record StatusIdeiaRequest(

        @NotNull(message = "Status é obrigatório")
        StatusIdeia status,

        // DECISION: justificativa aceita no contrato (seção 4) mas ainda
        // não persistida na ideia — o modelo de dados (seção 3) não tem
        // esse campo. Fica disponível para a Etapa F registrar no evento
        // de auditoria (APROVAR), sem inventar campo novo na entidade.
        String justificativa
) {
}
