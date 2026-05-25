package br.com.fiap.challenge_grupo_guia_branca.screens.dashbord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.challenge_grupo_guia_branca.model.Idea
import br.com.fiap.challenge_grupo_guia_branca.model.Projeto
import br.com.fiap.challenge_grupo_guia_branca.repository.ProjetoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(
    navController: NavController
) {

    var projetos by remember {
        mutableStateOf(listOf<Projeto>())
    }

    val projetoRepo = ProjetoRepository()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {

        scope.launch {
            projetos = projetoRepo.getProjetos()
        }
    }

    val totalProjetos = projetos.size

    val projetosConcluidos = projetos.count {
        it.projetoEncerrado
    }

    val investimentoTotal = projetos.sumOf {
        it.investimento
    }

    val receitaTotal = projetos.sumOf {
        it.receita
    }

    val lucroTotal = receitaTotal - investimentoTotal

    val roiMedio = if (investimentoTotal > 0) {
        ((lucroTotal / investimentoTotal) * 100)
    } else {
        0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    DashboardCard(
                        titulo = "Projetos",
                        valor = totalProjetos.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    DashboardCard(
                        titulo = "Concluídos",
                        valor = projetosConcluidos.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    DashboardCard(
                        titulo = "Investimento",
                        valor = "R$ %.2f".format(investimentoTotal),
                        modifier = Modifier.weight(1f)
                    )

                    DashboardCard(
                        titulo = "Lucro",
                        valor = "R$ %.2f".format(lucroTotal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {

                DashboardCard(
                    titulo = "ROI Médio",
                    valor = "%.2f%%".format(roiMedio),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {

                Text(
                    text = "Projetos",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(projetos) { projeto ->

                ProjetoResumoCard(projeto)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Voltar")
        }
    }
}

@Composable
fun DashboardCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun ProjetoResumoCard(
    projeto: Projeto
) {

    val lucro = projeto.receita - projeto.investimento

    val roi = if (projeto.investimento > 0) {
        ((lucro / projeto.investimento) * 100)
    } else {
        0.0
    }

    val dias = TimeUnit.MILLISECONDS.toDays(
        projeto.prazoInicial - System.currentTimeMillis()
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = projeto.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Investimento: R$ %.2f".format(projeto.investimento))

            Text("Receita: R$ %.2f".format(projeto.receita))

            Text("Lucro: R$ %.2f".format(lucro))

            Text("ROI: %.2f%%".format(roi))

            Text("Prazo: $dias dias")

            Text(
                text = if (projeto.projetoEncerrado)
                    "Concluído"
                else if (dias >= 0)
                    "Em andamento"
                else
                    "Atrasado"
            )
        }
    }
}
