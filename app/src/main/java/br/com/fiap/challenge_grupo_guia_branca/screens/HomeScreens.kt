package br.com.fiap.challenge_grupo_guia_branca.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OperadorHomeScreen() {
    RoleHomeScreen(
        title = "Area do Operador",
        description = "Aqui o operador vai cadastrar ideias e acompanhar seus status."
    )
}

@Composable
fun GestorHomeScreen() {
    RoleHomeScreen(
        title = "Area do Gestor",
        description = "Aqui o gestor vai avaliar ideias, priorizar e criar projetos."
    )
}

@Composable
fun LiderHomeScreen() {
    RoleHomeScreen(
        title = "Area da Lideranca",
        description = "Aqui a lideranca vai visualizar projetos, indicadores e resultados."
    )
}

@Composable
private fun RoleHomeScreen(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)
        Text(
            text = description,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
