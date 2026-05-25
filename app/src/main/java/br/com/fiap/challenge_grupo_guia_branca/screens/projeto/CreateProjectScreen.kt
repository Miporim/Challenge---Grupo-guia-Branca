package br.com.fiap.challenge_grupo_guia_branca.screens.projeto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.model.Projeto
import br.com.fiap.challenge_grupo_guia_branca.repository.ProjetoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    navController: NavController? = null,
    projetoId: String = ""
) {
    val repository = remember { ProjetoRepository() }
    val coroutineScope = rememberCoroutineScope()
    val isEditing = projetoId.isNotBlank()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var investimento by remember { mutableStateOf("") }
    var receita by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(projetoId) {
        if (isEditing) {
            isLoading = true
            val projeto = repository.getProjetoById(projetoId)
            projeto?.let {
                title = it.title
                description = it.description
                investimento = it.investimento.toString()
                receita = it.receita.toString()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Projeto" else "Criar Projeto") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título do Projeto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = investimento,
                onValueChange = { investimento = it },
                label = { Text("Investimento (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = receita,
                onValueChange = { receita = it },
                label = { Text("Receita Esperada (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            if (mensagem.isNotBlank()) {
                Text(
                    text = mensagem,
                    color = if (mensagem.startsWith("Erro"))
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        mensagem = "Preencha título e descrição"
                        return@Button
                    }
                    isLoading = true
                    mensagem = ""
                    coroutineScope.launch {
                        val projeto = Projeto(
                            id = if (isEditing) projetoId else "",
                            title = title.trim(),
                            description = description.trim(),
                            investimento = investimento.toDoubleOrNull() ?: 0.0,
                            receita = receita.toDoubleOrNull() ?: 0.0
                        )
                        val result = if (isEditing) {
                            repository.updateProjeto(projeto)
                        } else {
                            repository.createProjeto(projeto)
                        }
                        result
                            .onSuccess {
                                mensagem = if (isEditing) "Projeto atualizado!" else "Projeto criado!"
                                navController?.popBackStack()
                            }
                            .onFailure {
                                mensagem = "Erro: ${it.message}"
                            }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isEditing) "Salvar Alterações" else "Criar Projeto")
            }
        }
    }
}