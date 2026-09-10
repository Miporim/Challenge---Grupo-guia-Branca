package br.com.fiap.aguiabranca.ideia;

/**
 * Máquina de estados (seção 3): {@code RASCUNHO} → {@code SUBMETIDA} →
 * {@code EM_ANALISE} → {@code APROVADA} | {@code REPROVADA}.
 */
public enum StatusIdeia {
    RASCUNHO,
    SUBMETIDA,
    EM_ANALISE,
    APROVADA,
    REPROVADA
}
