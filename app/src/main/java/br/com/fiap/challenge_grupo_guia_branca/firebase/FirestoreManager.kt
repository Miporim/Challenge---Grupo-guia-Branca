package br.com.fiap.challenge_grupo_guia_branca.firebase

import br.com.fiap.challenge_grupo_guia_branca.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usersCollection = db.collection("users")

    /**
     * Cria o usuário no Firestore após o cadastro no Auth
     */
    suspend fun createUserInFirestore(
        userName: String,
        email: String,
        role: String
    ): Result<User> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuário não autenticado"))

        val user = User(
            id = currentUser.uid,
            userName = userName,
            email = email,
            role = role,
            createdAt = Date(),
            updatedAt = Date()
        )

        return try {
            usersCollection.document(currentUser.uid)
                .set(user, SetOptions.merge())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca dados completos do usuário logado
     */
    suspend fun getCurrentUser(): Result<User> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não logado"))

        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuário não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atualiza dados do usuário
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id)
                .set(user, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}