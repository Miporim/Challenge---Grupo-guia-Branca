package br.com.fiap.challenge_grupo_guia_branca.screens.ideia

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navArgument
import br.com.fiap.challenge_grupo_guia_branca.firebase.FirestoreManager
import br.com.fiap.challenge_grupo_guia_branca.model.Idea
import br.com.fiap.challenge_grupo_guia_branca.repository.IdeaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CreateIdeaScreen(
    navController: NavController,
    ideaId: String
) {

    // variáveis de estado
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var isNewIdea by remember {mutableStateOf(true)}

    // Acesso ao banco de dados
    val ideaRepository = IdeaRepository()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var idea = Idea()

    // Verifica se ideia é nova ou não
    isNewIdea = ideaId == ""
    if (!isNewIdea) {
        LaunchedEffect(Unit) {

            scope.launch {

                idea = ideaRepository.getIdeaById(ideaId)!!

                titulo = idea.title ?: ""

                descricao = idea.description ?: ""
            }
        }

    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Cadastrar Ideia")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = {
                titulo = it
            },
            label = {
                Text("Título")
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isNewIdea || (idea.totalVotes != 0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = {
                descricao = it
            },
            label = {
                Text("Descrição")
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isNewIdea || (idea.totalVotes != 0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (idea.totalVotes == 0) {
            Button(
                onClick = {

                    if (isNewIdea) {
                        scope.launch {
                            try {
                                val newIdea = Idea(

                                    title = titulo,

                                    description = descricao,
                                    userCreator = FirebaseAuth
                                        .getInstance()
                                        .currentUser
                                        ?.uid ?: ""
                                )

                                ideaRepository.createIdea(newIdea)

                                Toast.makeText(
                                    context,
                                    "Ideia salva com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate("home_operador")

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "Erro: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        scope.launch {

                            try {

                                val updatedIdea = Idea(

                                    id = ideaId,

                                    title = titulo,

                                    description = descricao,

                                    userCreator = FirebaseAuth
                                        .getInstance()
                                        .currentUser
                                        ?.uid ?: ""
                                )

                                ideaRepository.updateIdea(updatedIdea)

                                Toast.makeText(
                                    context,
                                    "Ideia modificada com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {

                                Log.e("FIREBASE", e.message.toString())
                            }
                        }
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Salvar Ideia")
            }
            if (!isNewIdea && idea.totalVotes == 0) {
                Button(
                    onClick = {
                        scope.launch {

                            try {
                                Log.e("FIREBASE", ideaId)
                                ideaRepository.deleteIdea(ideaId)

                                Toast.makeText(
                                    context,
                                    "Ideia removida!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.popBackStack()

                            } catch (e: Exception) {
                                Log.e("FIREBASE", e.message.toString())
                                Toast.makeText(
                                    context,
                                    e.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Text("Deletar Ideia")
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
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

