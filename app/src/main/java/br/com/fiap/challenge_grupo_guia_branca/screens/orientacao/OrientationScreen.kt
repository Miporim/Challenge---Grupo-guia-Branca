package br.com.fiap.challenge_grupo_guia_branca.screens.orientacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.firebase.AuthManager
import br.com.fiap.challenge_grupo_guia_branca.model.Orientacao
import br.com.fiap.challenge_grupo_guia_branca.model.User
import br.com.fiap.challenge_grupo_guia_branca.repository.OrientacaoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrientationScreen(
    navController: NavController? = null
) {
    val repository = remember { OrientacaoRepository() }
    val coroutineScope = rememberCoroutineScope()
    val currentRole = AuthManager.getCurrentUserRole()
    val isLider = currentRole == User.ROLE_LIDER

    var orientacoes by remember { mutableStateOf<List<Orientacao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var orientacaoEmEdicao by remember { mutableStateOf<Orientacao?>(null) }
    var orientacaoParaDeletar by remember { mutableStateOf<Orientacao?>(null) }

    fun loadOrientacoes() {
        coroutineScope.launch {
            isLoading = true
            orientacoes = repository.getOrientacoes()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadOrientacoes() }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; orientacaoEmEdicao = null },
            title = { Text(if (orientacaoEmEdicao != null) "Editar Orientação" else "Nova Orientação") },
            text = {
                var title by remember { mutableStateOf(orientacaoEmEdicao?.title ?: "") }
                var description by remember { mutableStateOf(orientacaoEmEdicao?.description ?: "") }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val orientacao = orientacaoEmEdicao?.copy() ?: Orientacao()
                        if (orientacaoEmEdicao != null) {
                            repository.updateOrientacao(orientacao)
                        } else {
                            repository.createOrientacao(orientacao)
                        }
                        showDialog = false
                        orientacaoEmEdicao = null
                        loadOrientacoes()
                    }
                }) { Text(if (orientacaoEmEdicao != null) "Salvar" else "Criar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; orientacaoEmEdicao = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    orientacaoParaDeletar?.let { orientacao ->
        AlertDialog(
            onDismissRequest = { orientacaoParaDeletar = null },
            title = { Text("Excluir Orientação") },
            text = { Text("Tem certeza que deseja excluir \"${orientacao.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        repository.deleteOrientacao(orientacao.id)
                        orientacaoParaDeletar = null
                        loadOrientacoes()
                    }
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { orientacaoParaDeletar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orientações Estratégicas") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isLider) {
                FloatingActionButton(onClick = {
                    orientacaoEmEdicao = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Orientação")
                }
            }
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (orientacoes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhuma orientação cadastrada.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(orientacoes) { orientacao ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = orientacao.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = orientacao.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isLider) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { orientacaoEmEdicao = orientacao; showDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { orientacaoParaDeletar = orientacao }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}