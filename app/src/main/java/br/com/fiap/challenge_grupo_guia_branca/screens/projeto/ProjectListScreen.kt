package br.com.fiap.challenge_grupo_guia_branca.screens.projeto

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.firebase.AuthManager
import br.com.fiap.challenge_grupo_guia_branca.model.Projeto
import br.com.fiap.challenge_grupo_guia_branca.model.User
import br.com.fiap.challenge_grupo_guia_branca.repository.ProjetoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    navController: NavController? = null
) {
    val repository = remember { ProjetoRepository() }
    val coroutineScope = rememberCoroutineScope()

    var projetos by remember { mutableStateOf<List<Projeto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var projetoParaDeletar by remember { mutableStateOf<Projeto?>(null) }

    val currentRole = AuthManager.getCurrentUserRole()
    val isGestor = currentRole == User.ROLE_GESTOR

    LaunchedEffect(Unit) {
        projetos = repository.getProjetos()
        isLoading = false
    }

    projetoParaDeletar?.let { projeto ->
        AlertDialog(
            onDismissRequest = { projetoParaDeletar = null },
            title = { Text("Excluir Projeto") },
            text = { Text("Tem certeza que deseja excluir \"${projeto.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            Log.e("FIREBASE", projeto.userCreator)
                            Log.e("FIREBASE", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                            repository.deleteProjeto(projeto.id)
                            projetos = repository.getProjetos()
                            projetoParaDeletar = null
                        }
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { projetoParaDeletar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projetos") },
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
            if (isGestor) {
                FloatingActionButton(
                    onClick = { navController?.navigate("create_project") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Criar Projeto")
                }
            }
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (projetos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum projeto cadastrado.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(projetos) { projeto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = projeto.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = projeto.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Investimento: R$ ${"%.2f".format(projeto.investimento)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Receita: R$ ${"%.2f".format(projeto.receita)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isGestor) {
                                Row {
                                    if (!projeto.projetoEncerrado) {
                                        IconButton(
                                            onClick = {
                                                navController?.navigate("edit_project/${projeto.id}")
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { projetoParaDeletar = projeto }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    else {
                                        IconButton(
                                            onClick = {
                                                navController?.navigate("edit_project/${projeto.id}")
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}