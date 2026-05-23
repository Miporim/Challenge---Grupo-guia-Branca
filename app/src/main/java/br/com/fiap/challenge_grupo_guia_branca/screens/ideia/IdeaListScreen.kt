package br.com.fiap.challenge_grupo_guia_branca.screens.ideia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.challenge_grupo_guia_branca.model.Ideia
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun IdeaListScreen() {

    var ideias by remember {
        mutableStateOf(listOf<Ideia>())
    }

    val db = Firebase.firestore

    LaunchedEffect(Unit) {

        db.collection("ideias")
            .get()
            .addOnSuccessListener { result ->

                ideias = result.documents.mapNotNull {
                    it.toObject(Ideia::class.java)
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items(ideias) { ideia ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(text = ideia.titulo)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = ideia.descricao)
                }
            }
        }
    }
}