package br.com.fiap.challenge_grupo_guia_branca.model

data class Projeto(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    var userCreator: String = "",
    val investimento: Double = 0.0,
    val receita: Double = 0.0,
    val prazoInicial: Long = System.currentTimeMillis(),
    val dataConclusao: Long = System.currentTimeMillis(),
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)