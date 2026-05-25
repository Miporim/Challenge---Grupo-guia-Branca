package br.com.fiap.challenge_grupo_guia_branca.repository

import android.util.Log
import br.com.fiap.challenge_grupo_guia_branca.model.Projeto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProjetoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("projetos")


    suspend fun createProjeto(projeto: Projeto): Result<Unit> {
        return try {
            val document = collection.document()
            projeto.id = document.id
            projeto.userCreator = auth.currentUser?.uid ?: ""
            projeto.createdAt = System.currentTimeMillis()
            projeto.updatedAt = System.currentTimeMillis()
            document.set(projeto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao criar projeto: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getProjetos(): List<Projeto> {
        return try {
            collection
                .get()
                .await()
                .toObjects(Projeto::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao buscar projetos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getProjetosByUserId(
        userId: String
    ): List<Projeto> {
        return try {
            collection
                .whereEqualTo("userCreator", userId)
                .get()
                .await()
                .toObjects(Projeto::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao buscar projetos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getProjetoById(projetoId: String): Projeto? {
        return try {
            collection
                .document(projetoId)
                .get()
                .await()
                .toObject(Projeto::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao buscar projeto: ${e.message}")
            null
        }
    }
    suspend fun updateProjeto(projeto: Projeto): Result<Unit> {
        return try {
            projeto.updatedAt = System.currentTimeMillis()
            collection
                .document(projeto.id)
                .set(projeto)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao atualizar projeto: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteProjeto(projetoId: String): Result<Unit> {
        return try {
            collection
                .document(projetoId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Erro ao deletar projeto: ${e.message}")
            Result.failure(e)
        }
    }
}