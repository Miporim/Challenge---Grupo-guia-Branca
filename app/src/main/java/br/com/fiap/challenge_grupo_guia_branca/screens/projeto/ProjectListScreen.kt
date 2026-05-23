package br.com.fiap.challenge_grupo_guia_branca.screens.projeto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Projeto(
    val titulo: String,
    val descricao: String,
    val investimento: String,
    val receita: String
)

@Composable
fun ProjectListScreen() {

    val projetos = remember {

        mutableStateListOf(

            Projeto(
                "Sistema de Frota",
                "Controle inteligente da frota",
                "50000",
                "120000"
            ),

            Projeto(
                "Automação",
                "Automação de processos",
                "30000",
                "90000"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items(projetos) { projeto ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(text = projeto.titulo)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = projeto.descricao)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Investimento: ${projeto.investimento}")

                    Text(text = "Receita: ${projeto.receita}")
                }
            }
        }
    }
}