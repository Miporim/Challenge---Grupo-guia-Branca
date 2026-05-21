package br.com.fiap.challenge_grupo_guia_branca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.fiap.challenge_grupo_guia_branca.navigation.AppNavigation
import br.com.fiap.challenge_grupo_guia_branca.ui.theme.ChallengeGrupoguiaBrancaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            ChallengeGrupoguiaBrancaTheme {

                AppNavigation()

            }
        }
    }
}