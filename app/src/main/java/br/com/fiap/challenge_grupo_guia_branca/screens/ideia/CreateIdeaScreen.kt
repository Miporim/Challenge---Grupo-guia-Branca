package br.com.fiap.challenge_grupo_guia_branca.screens.ideia

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
import androidx.compose.ui.unit.dp
import br.com.fiap.challenge_grupo_guia_branca.firebase.FirestoreManager
import kotlinx.coroutines.launch

@Composable
fun CreateIdeaScreen() {

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()
    var mensagem by remember { mutableStateOf("") }


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
            modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                coroutineScope.launch {

                    firestoreManager.saveIdea(
                        titulo,
                        descricao
                    )
                        .onSuccess {

                            mensagem = "Ideia salva com sucesso!"

                            titulo = ""
                            descricao = ""
                        }

                        .onFailure {

                            mensagem = it.message ?: "Erro ao salvar ideia"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Salvar Ideia")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = mensagem)
    }
}

