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
            OperadorHomeScreen()
        }

        composable("home_gestor") {
            GestorHomeScreen()
        }

        composable("home_lider") {
            LiderHomeScreen()
        }
    }
}
