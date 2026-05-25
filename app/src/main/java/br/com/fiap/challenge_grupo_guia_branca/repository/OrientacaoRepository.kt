package br.com.fiap.challenge_grupo_guia_branca.repository

import android.util.Log
import br.com.fiap.challenge_grupo_guia_branca.model.Orientacao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrientacaoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("orientacoes")

    // CREATE
    suspend fun createOrientacao(orientacao: Orientacao): Result<Unit> {
        return try {
            val document = collection.document()
            orientacao.id = document.id
            orientacao.userCreator = auth.currentUser?.uid ?: ""
            orientacao.createdAt = System.currentTimeMillis()
            orientacao.updatedAt = System.currentTimeMillis()
            document.set(orientacao).await()
            Log.e("FIREBASE", "Criou certo: $orientacao")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao criar orientacao: ${e.message}")
            Result.failure(e)
        }
    }

    // READ - todas as orientacoes
    suspend fun getOrientacoes(): List<Orientacao> {
        return try {
            collection
                .get()
                .await()
                .toObjects(Orientacao::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao buscar orientacoes: ${e.message}")
            emptyList()
        }
    }

    // READ - orientacao por ID
    suspend fun getOrientacaoById(orientacaoId: String): Orientacao? {
        return try {
            collection
                .document(orientacaoId)
                .get()
                .await()
                .toObject(Orientacao::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao buscar orientacao: ${e.message}")
            null
        }
    }

    // UPDATE
    suspend fun updateOrientacao(orientacao: Orientacao): Result<Unit> {
        return try {
            orientacao.updatedAt = System.currentTimeMillis()
            collection
                .document(orientacao.id)
                .set(orientacao)
                .await()
            Log.e("FIREBASE", "Editou certo: $orientacao")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao atualizar orientacao: ${e.message}")
            Result.failure(e)
        }
    }

    // DELETE
    suspend fun deleteOrientacao(orientacaoId: String): Result<Unit> {
        return try {
            collection
                .document(orientacaoId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao deletar orientacao: ${e.message}")
            Result.failure(e)
        }
    }
}
