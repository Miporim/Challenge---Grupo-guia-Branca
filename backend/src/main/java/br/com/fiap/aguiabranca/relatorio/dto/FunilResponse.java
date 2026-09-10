package br.com.fiap.aguiabranca.relatorio.dto;

public record FunilResponse(
        long submetidas,
        long aprovadas,
        long viraramProjeto,
        long concluidas
) {
}
