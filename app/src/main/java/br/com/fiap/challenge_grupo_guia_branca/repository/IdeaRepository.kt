package br.com.fiap.challenge_grupo_guia_branca.repository;

import android.util.Log
import br.com.fiap.challenge_grupo_guia_branca.model.Idea
import br.com.fiap.challenge_grupo_guia_branca.model.PriorityIdea
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

    suspend fun getIdeas(): List<Idea> {

        return collection
            .get()
            .await()
            .toObjects(Idea::class.java)
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

    suspend fun updateIdeaScore(
        ideaId: String
    ) {

        val priorityVotes = collection
            .document(ideaId)
            .collection("priorityVote")
            .get()
            .await()

        var total = 0

        for (document in priorityVotes.documents) {

            val vote =
                document.toObject(PriorityIdea::class.java)

            total += vote?.score ?: 0
        }

        collection
            .document(ideaId)
            .update(

                mapOf(
                    "scoreTotal" to total,
                    "totalVotes" to priorityVotes.size()
                )
            )
            .await()
    }

    suspend fun voteIdea(

        ideaId: String,

        gestorId: String,

        score: Int
    ) {

        val priorityIdea = PriorityIdea(

            gestorId = gestorId,

            score = score
        )

        collection
            .document(ideaId)
            .collection("priorityVote")
            .document(gestorId)
            .set(priorityIdea)
            .await()

        updateIdeaScore(ideaId)
    }
}