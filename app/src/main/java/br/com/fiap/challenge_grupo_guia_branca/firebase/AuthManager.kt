package br.com.fiap.challenge_grupo_guia_branca.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

object AuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun register(
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erro")
            }
    }

    fun login(
        email: String,
        senha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erro")
            }
    }

    fun updateCurrentUserProfile(
        nome: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("Usuario nao autenticado")
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("$nome|$role")
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erro ao salvar perfil")
            }
    }

    fun getCurrentUserRole(): String? {
        return auth.currentUser
            ?.displayName
            ?.substringAfter("|", missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
    }

    fun deleteCurrentUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onSuccess()
            return
        }

        currentUser.delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erro ao desfazer cadastro")
            }
    }
    fun logout() {

        FirebaseAuth.getInstance().signOut()
    }
}
