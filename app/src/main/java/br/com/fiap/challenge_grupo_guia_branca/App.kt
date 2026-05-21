package br.com.fiap.challenge_grupo_guia_branca

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}