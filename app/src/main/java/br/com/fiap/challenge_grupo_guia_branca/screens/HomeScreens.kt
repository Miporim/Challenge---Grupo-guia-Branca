package br.com.fiap.challenge_grupo_guia_branca.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OperadorHomeScreen(navController: NavController) {
    RoleHomeScreen(
        title = "Area do Operador"
    ) {
        HomeButton(
            text = "Adicionar ideia",
            onClick = { navController.navigate("operador_adicionar_ideia") }
        )
        HomeButton(
            text = "Minhas ideias",
            onClick = { navController.navigate("operador_minhas_ideias") }
        )
        HomeButton(
            text = "Sair",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

@Composable
fun GestorHomeScreen(navController: NavController) {
    RoleHomeScreen(
        title = "Area do Gestor"
    ) {
        HomeButton(
            text = "Ver ideias dos operadores",
            onClick = { navController.navigate("gestor_ideias_operadores") }
        )
        HomeButton(
            text = "Criar projeto",
            onClick = { navController.navigate("gestor_criar_projeto") }
        )
        HomeButton(
            text = "Ver meus projetos",
            onClick = { navController.navigate("gestor_meus_projetos") }
        )
        HomeButton(
            text = "Sair",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

@Composable
fun LiderHomeScreen(navController: NavController) {
    RoleHomeScreen(
        title = "Area da Lideranca"
    ) {
        HomeButton(
            text = "Dashboard",
            onClick = { navController.navigate("lider_dashboard") }
        )
        HomeButton(
            text = "Ver Projetos",
            onClick = { navController.navigate("lider_projetos") }
        )
        HomeButton(
            text = "Indicadores",
            onClick = { navController.navigate("lider_indicadores") }
        )
        HomeButton(
            text = "Sair",
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

@Composable
private fun RoleHomeScreen(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title)
        content()
    }
}

@Composable
private fun HomeButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun TemporaryFeatureScreen(
    navController: NavController,
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
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Voltar")
        }
    }
}
