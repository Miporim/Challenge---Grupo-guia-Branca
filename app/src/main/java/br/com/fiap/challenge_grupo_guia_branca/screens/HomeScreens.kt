package br.com.fiap.challenge_grupo_guia_branca.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.firebase.AuthManager

@Composable
fun OperadorHomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Área do Operador")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("create_idea")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar Ideia")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("idea_list_operador")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Minhas Ideias")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("orientations")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Orientações Estratégicas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                AuthManager.logout()

                navController.navigate("login") {

                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Sair")
        }
    }
}

@Composable
fun GestorHomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Área do Gestor")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("project_list")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Projetos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("create_project")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Projeto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("idea_list_gestor")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Avaliar Ideias")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                AuthManager.logout()

                navController.navigate("login") {

                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Sair")
        }
    }
}

@Composable
fun LiderHomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Área da Liderança")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("dashboard")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dashboard")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("project_list")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Projetos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("orientations")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Orientações Estratégicas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                AuthManager.logout()

                navController.navigate("login") {

                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Sair")
        }
    }
}