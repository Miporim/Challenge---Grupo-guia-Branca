package br.com.fiap.challenge_grupo_guia_branca.model

data class PriorityIdea (
    var gestorId: String = "",
    var score: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)