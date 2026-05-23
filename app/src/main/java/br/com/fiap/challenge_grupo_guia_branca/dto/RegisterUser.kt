package br.com.fiap.challenge_grupo_guia_branca.dto

data class RegisterUser(
    val userName: String = "",
    val email: String = "",
    val senha: String = "",
    val role: String = ""
)