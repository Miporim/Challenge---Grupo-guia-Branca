package br.com.fiap.challenge_grupo_guia_branca.auth

import android.content.Context

class SessionManager(context: Context) {

    private val tokenManager = TokenManager(context)

    fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    fun clearSession() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }
}