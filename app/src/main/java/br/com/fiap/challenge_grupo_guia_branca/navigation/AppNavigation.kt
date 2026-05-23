package br.com.fiap.challenge_grupo_guia_branca.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.fiap.challenge_grupo_guia_branca.screens.GestorHomeScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.LiderHomeScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.LoginScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.OperadorHomeScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.RegisterScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.TemporaryFeatureScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("home_operador") {
            OperadorHomeScreen(navController)
        }

        composable("home_gestor") {
            GestorHomeScreen(navController)
        }

        composable("home_lider") {
            LiderHomeScreen(navController)
        }

        composable("operador_adicionar_ideia") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Adicionar ideia",
                description = "Tela onde o operador vai cadastrar uma nova ideia ou problema."
            )
        }

        composable("operador_minhas_ideias") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Minhas ideias",
                description = "Tela onde o operador vai consultar o historico das ideias cadastradas."
            )
        }

        composable("gestor_ideias_operadores") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Ideias dos operadores",
                description = "Tela onde o gestor vai visualizar e avaliar as ideias cadastradas."
            )
        }

        composable("gestor_criar_projeto") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Criar projeto",
                description = "Tela onde o gestor vai criar um projeto a partir de uma ideia aprovada."
            )
        }

        composable("lider_dashboard") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Dashboard",
                description = "Tela onde a lideranca vai acompanhar a visao geral dos resultados."
            )
        }

        composable("lider_projetos") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Ver Projetos",
                description = "Tela onde a lideranca vai consultar os projetos em andamento."
            )
        }

        composable("lider_indicadores") {
            TemporaryFeatureScreen(
                navController = navController,
                title = "Indicadores",
                description = "Tela onde a lideranca vai visualizar indicadores de impacto e ROI."
            )
        }
    }
}
