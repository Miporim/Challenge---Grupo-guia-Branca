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
import br.com.fiap.challenge_grupo_guia_branca.screens.ideia.CreateIdeaScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.ideia.IdeaListScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.dashbord.DashboardScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.projeto.CreateProjectScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.projeto.ProjectListScreen
import br.com.fiap.challenge_grupo_guia_branca.screens.orientacao.OrientationScreen

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

        composable("create_idea") {
            CreateIdeaScreen()
        }

        composable("idea_list") {
            IdeaListScreen()
        }

        composable("dashboard") {
            DashboardScreen()
        }

        composable("create_project") {
            CreateProjectScreen()
        }

        composable("project_list") {
            ProjectListScreen()
        }

        composable("orientations") {
            OrientationScreen()
        }
    }
}
