package br.com.fiap.challenge_grupo_guia_branca.screens.orientacao

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

data class Orientacao(
    val titulo: String,
    val descricao: String
)

@Composable
fun OrientationScreen() {

    val orientacoes = remember {

        mutableStateListOf(

            Orientacao(
                "Redução de Custos",
                "Buscar redução de custos operacionais em 2026."
            ),

            Orientacao(
                "Inovação",
                "Estimular ideias inovadoras dos operadores."
            ),

            Orientacao(
                "Produtividade",
                "Melhorar processos internos da empresa."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items(orientacoes) { orientacao ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(text = orientacao.titulo)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = orientacao.descricao)
                }
            }
        }
    }
}