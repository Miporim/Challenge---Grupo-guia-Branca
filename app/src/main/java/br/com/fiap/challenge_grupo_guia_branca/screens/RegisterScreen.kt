package br.com.fiap.challenge_grupo_guia_branca.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.model.User
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import br.com.fiap.challenge_grupo_guia_branca.api.RetrofitClient
import br.com.fiap.challenge_grupo_guia_branca.dto.RegisterUser

@Composable
fun RegisterScreen(navController: NavController) {

    val context = LocalContext.current

    val apiService = remember {
        RetrofitClient.create(context)
    }
    val coroutineScope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(User.ROLE_OPERADOR) }
    var mensagem by remember { mutableStateOf("") }

    val roles = listOf(
        User.ROLE_OPERADOR to "Operador",
        User.ROLE_GESTOR to "Gestor",
        User.ROLE_LIDER to "Lider"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Cadastro")

        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
            },
            label = {
                Text("Nome")
            }
        )

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

        Text(
            text = "Tipo de usuario",
            modifier = Modifier.padding(top = 12.dp)
        )

        roles.forEach { (roleValue, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = role == roleValue,
                    onClick = {
                        role = roleValue
                    }
                )
                Text(text = label)
            }
        }

        Button(
            onClick = {

                if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                    mensagem = "Preencha nome, email e senha"
                    return@Button
                }

                if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
                    mensagem = "Preencha nome, email e senha"
                    return@Button
                }

                coroutineScope.launch {

                    try {

                        val response = apiService.register(
                            RegisterUser(
                                name = nome.trim(),
                                email = email.trim(),
                                password = senha,
                                role = role
                            )
                        )

                        if (response.isSuccessful) {

                            mensagem = "Conta criada com sucesso"

                            navController.navigate("login") {
                                popUpTo("register") {
                                    inclusive = true
                                }
                            }

                        } else {

                            mensagem = when (response.code()) {

                                409 -> "E-mail já cadastrado"

                                400 -> "Dados inválidos"

                                else -> "Erro ao criar conta: ${response.code()}"
                            }
                        }

                    } catch (e: Exception) {

                        mensagem = "Erro ao conectar com a API: ${e.message}"
                    }
                }
            }
        ) {

            Text(text = "Cadastrar")
        }

        if (mensagem.isNotBlank()) {
            Text(
                text = mensagem,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        TextButton(
            onClick = {
                navController.navigate("login")
            }
        ) {

            Text(text = "Voltar")
        }
    }
}
