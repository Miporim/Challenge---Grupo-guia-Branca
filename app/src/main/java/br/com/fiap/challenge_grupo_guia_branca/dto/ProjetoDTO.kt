package br.com.fiap.challenge_grupo_guia_branca.dto

data class ProjetoDTO(
    val title: String = "",
    val description: String = "",
    val investimento: Double = 0.0,
    val receita: Double = 0.0,
    val prazoInicial: Long = System.currentTimeMillis(),
    val dataConclusao: Long = System.currentTimeMillis()
)