package br.com.fiap.challenge_grupo_guia_branca.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.firebase.AuthManager
import br.com.fiap.challenge_grupo_guia_branca.firebase.FirestoreManager
import br.com.fiap.challenge_grupo_guia_branca.model.User
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {

    val firestoreManager = remember { FirestoreManager() }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Login")

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            }
        )

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
            },
            label = {
                Text("Senha")
            }
        )

        Button(
            onClick = {
                if (email.isBlank() || senha.isBlank()) {
                    mensagem = "Preencha email e senha"
                    return@Button
                }

                mensagem = "Entrando..."

                AuthManager.login(
                    email,
                    senha,

                    onSuccess = {
                        coroutineScope.launch {
                            firestoreManager.getCurrentUser()
                                .onSuccess { user ->
                                    val destination = when (user.role) {
                                        User.ROLE_OPERADOR -> "home_operador"
                                        User.ROLE_GESTOR -> "home_gestor"
                                        User.ROLE_LIDER -> "home_lider"
                                        else -> "home_operador"
                                    }

                                    navController.navigate(destination) {
                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                }
                                .onFailure {
                                    val role = AuthManager.getCurrentUserRole()
                                    val destination = when (role) {
                                        User.ROLE_OPERADOR -> "home_operador"
                                        User.ROLE_GESTOR -> "home_gestor"
                                        User.ROLE_LIDER -> "home_lider"
                                        else -> null
                                    }

                                    if (destination != null) {
                                        navController.navigate(destination) {
                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    } else {
                                        mensagem = "Login feito, mas o perfil do usuario nao foi encontrado."
                                    }
                                }
                        }
                    },

                    onError = {
                        mensagem = it
                    }
                )

            }
        ) {

            Text(text = "Entrar")
        }

        if (mensagem.isNotBlank()) {
            Text(
                text = mensagem,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        TextButton(
            onClick = {
                navController.navigate("register")
            }
        ) {

            Text(text = "Criar conta")
        }
    }
}
