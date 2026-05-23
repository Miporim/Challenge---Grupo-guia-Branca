package br.com.fiap.challenge_grupo_guia_branca.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserRepository {

    private val auth = Firebase.auth

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}