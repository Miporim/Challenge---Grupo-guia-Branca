// User.kt
package br.com.fiap.challenge_grupo_guia_branca.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val id: String = "",                    // UID do Firebase Auth
    val userName: String = "",
    val email: String = "",
    val role: String = "",                  // "OPERADOR", "GESTOR", "LIDER"
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    companion object {
        const val ROLE_OPERADOR = "OPERADOR"
        const val ROLE_GESTOR = "GESTOR"
        const val ROLE_LIDER = "LIDER"
    }

    fun isOperador() = role == ROLE_OPERADOR
    fun isGestor() = role == ROLE_GESTOR
    fun isLider() = role == ROLE_LIDER
}