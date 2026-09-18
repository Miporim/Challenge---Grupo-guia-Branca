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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.model.User
import kotlinx.coroutines.launch
import br.com.fiap.challenge_grupo_guia_branca.api.RetrofitClient
import br.com.fiap.challenge_grupo_guia_branca.auth.TokenManager
import br.com.fiap.challenge_grupo_guia_branca.dto.LoginDTO

@Composable
fun LoginScreen(navController: NavController) {

    // Dependências da API

    val context = LocalContext.current

    val apiService = remember {
        RetrofitClient.create(context)
    }

    val tokenManager = remember {
        TokenManager(context)
    }
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

                coroutineScope.launch {

                    try {

                        val response = apiService.login(
                            LoginDTO(
                                email = email,
                                password = senha
                            )
                        )

                        tokenManager.saveToken(response.token)

                        val user = apiService.me()

                        val destination = when (user.role) {
                            User.ROLE_OPERADOR -> "home_operador"
                            User.ROLE_GESTOR -> "home_gestor"
                            User.ROLE_LIDER -> "home_lider"
                            else -> {
                                mensagem = "Perfil de usuário inválido."
                                return@launch
                            }
                        }

                        navController.navigate(destination) {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }

                    } catch (e: Exception) {

                        mensagem = when {
                            e.message?.contains("401") == true ->
                                "E-mail ou senha inválidos."

                            else ->
                                "Erro ao conectar com a API: ${e.message}"
                        }
                    }
                }

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
