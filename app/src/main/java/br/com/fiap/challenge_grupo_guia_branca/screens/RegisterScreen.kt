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
import br.com.fiap.challenge_grupo_guia_branca.firebase.AuthManager
import br.com.fiap.challenge_grupo_guia_branca.firebase.FirestoreManager
import br.com.fiap.challenge_grupo_guia_branca.model.User
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {

    val firestoreManager = remember { FirestoreManager() }
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

                mensagem = "Criando conta..."

                AuthManager.register(
                    email,
                    senha,

                    onSuccess = {
                        AuthManager.updateCurrentUserProfile(
                            nome = nome,
                            role = role,
                            onSuccess = {
                                coroutineScope.launch {
                                    val result = firestoreManager.createUserInFirestore(
                                        userName = nome,
                                        email = email,
                                        role = role
                                    )

                                    result
                                        .onSuccess {
                                            mensagem = "Conta criada com sucesso"
                                            navController.navigate("login") {
                                                popUpTo("register") {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                        .onFailure {
                                            mensagem = "Conta criada. Firestore bloqueado, mas a role foi salva no Auth."
                                            navController.navigate("login") {
                                                popUpTo("register") {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                }
                            },
                            onError = {
                                AuthManager.deleteCurrentUser(
                                    onSuccess = {
                                        mensagem = "Conta nao criada: erro ao salvar perfil."
                                    },
                                    onError = { deleteError ->
                                        mensagem = "Usuario criado, mas sem perfil: $deleteError"
                                    }
                                )
                            }
                        )
                    },

                    onError = {
                        mensagem = it
                    }
                )

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
