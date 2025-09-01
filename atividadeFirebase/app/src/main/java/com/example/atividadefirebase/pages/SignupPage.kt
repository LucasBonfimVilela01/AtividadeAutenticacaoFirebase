package com.example.atividadefirebase.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.atividadefirebase.AuthState
import com.example.atividadefirebase.AuthViewModel

@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate("home") {
                    popUpTo("signup") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is AuthState.Error -> Toast.makeText(
                context,
                state.message, Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cadastro",
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier
                .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
                .border(2.dp, Color.Black, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField( // Novo campo para Nome de Exibição
            value = displayName,
            onValueChange = { displayName = it }, // Atualizar estado do nome de exibição
            label = { Text("Nome de exibição") },
            shape = RoundedCornerShape(32.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(32.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            shape = RoundedCornerShape(32.dp),
            visualTransformation = PasswordVisualTransformation(), // Esconder senha
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp)) // Espaçamento aumentado

        Button(
            onClick = {
                // Passar displayName para a função de cadastro
                authViewModel.signup(displayName, email, password)
            },
            enabled = authState.value != AuthState.Loading
        ) {
            Text(text = "Cadastrar") // "Cadastrar" em Português
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true } // Opcional: remover a si mesmo se for para o login
                launchSingleTop = true
            }
        }) {
            Text(text = "Já possui conta? Faça login") // "Already have an account? Log in"
        }
    }
}
