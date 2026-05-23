package br.com.fiap.challenge_grupo_guia_branca.repository;

import android.util.Log
import br.com.fiap.challenge_grupo_guia_branca.model.Idea
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IdeaRepository {

    private val db = FirebaseFirestore.getInstance();

    private val collection = db.collection("ideas")

    fun createIdea(
        idea: Idea
    ) {

        val document = collection.document()

        idea.id = document.id

        document.set(idea)
            .addOnSuccessListener {

                Log.d("FIREBASE", "SALVOU")
            }

            .addOnFailureListener {

                Log.e("FIREBASE", it.message.toString())
            }
    }

    suspend fun getIdeasByUser(
        userId: String
    ): List<Idea> {

        return collection
            .whereEqualTo("userCreator", userId)
            .get()
            .await()
            .toObjects(Idea::class.java)
    }

    suspend fun getIdeaById(
        ideaId: String
    ): Idea? {

        return collection
            .document(ideaId)
            .get()
            .await()
            .toObject(Idea::class.java)
    }

    suspend fun updateIdea(
        idea: Idea
    ) {

        collection
            .document(idea.id)
            .set(idea)
            .await()
    }

    suspend fun deleteIdea(
        ideaId: String
    ) {

        collection
            .document(ideaId)
            .delete()
            .await()
    }
}