package com.example.atividadefirebase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.atividadefirebase.ui.theme.AtividadeFirebaseTheme
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilitar persistência ANTES de outras chamadas do database
        try {
            Firebase.database.setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao habilitar persistência ou keepSynced: ${e.message}")
            // Isso pode acontecer se setPersistenceEnabled for chamado depois de obter uma instância do database
        }

        enableEdgeToEdge()
        val authViewModel = AuthViewModel()
        setContent {
            AtividadeFirebaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel)
                }
            }
        }
    }
}
