package br.com.fiap.challenge_grupo_guia_branca.model

data class Orientacao(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    var userCreator: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)