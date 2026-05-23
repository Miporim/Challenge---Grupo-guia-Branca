package br.com.fiap.challenge_grupo_guia_branca.screens.ideia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.model.Idea
import br.com.fiap.challenge_grupo_guia_branca.repository.IdeaRepository
import br.com.fiap.challenge_grupo_guia_branca.utils.formatDate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants

@Composable
fun IdeaListScreen(
    navController: NavController
) {

    // Lista de ideias
    var ideas by remember {
        mutableStateOf(listOf<Idea>())
    }

    // Acesso ao banco de dados
    val ideaRepository = IdeaRepository()

    val scope = rememberCoroutineScope()

    // Popula a lista ao chamar a tela
    LaunchedEffect(Unit) {

        scope.launch {

            val uid = FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: ""

            ideas = ideaRepository.getIdeasByUser(uid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(text = "MINHAS IDEIAS")

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(ideas) { idea ->

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {

                            navController.navigate(
                                "edit_idea/${idea.id}"
                            )
                        }
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = idea.title,
                            fontWeight = FontWeight.Bold
                        )

                        if (idea.description.length > 25) {
                            Text(
                                text = idea.description.substring(0, 22) + "..."
                            )
                        }
                        else {
                            Text(
                                text = idea.description
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(formatDate(idea.createdAt))
                            Text("Avalição: " + idea.scoreTotal.toString())
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Voltar")
        }
    }
}