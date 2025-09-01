package com.example.atividadefirebase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.atividadefirebase.R
import com.example.atividadefirebase.AuthState
import com.example.atividadefirebase.AuthViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val currentUser: FirebaseUser? = authViewModel.getCurrentUser()

    LaunchedEffect(authState.value) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (authState.value is AuthState.Unauthenticated && currentRoute != "login" && currentRoute != "signup") {
            navController.navigate("login") { // Navega para a tela de login
                popUpTo(navController.graph.startDestinationId) { // Limpa a pilha de volta para evitar voltar para casa
                    inclusive = true
                }
                launchSingleTop = true // Evita múltiplas instâncias de login
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF87CEEB), Color(0xFFFFC0CB))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, // Distribui o espaço
                modifier = Modifier
                    .fillMaxWidth() // Faz a Row ocupar toda a largura
                    .padding(horizontal = 16.dp, vertical = 16.dp) // Adiciona preenchimento
            ) {
                Text(
                    text = "Página Inicial", // Texto traduzido
                    fontSize = 32.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center, // Centraliza o texto dentro do Text composable
                    modifier = Modifier
                        .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
                        .border(2.dp, Color.Black, shape = RoundedCornerShape(16.dp))
                        .padding(
                            horizontal = 24.dp,
                            vertical = 12.dp
                        ) // Mantém o preenchimento para o título
                        .weight(1f) // Permite que o título ocupe o espaço disponível, centralizando-o efetivamente
                )
                Image(
                    painter = painterResource(id = R.drawable.pfp_icon), // Ícone da sua foto de perfil
                    contentDescription = "Minha Conta", // Descrição traduzida
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp)) // Torna circular
                        .clickable {
                            if (authState.value is AuthState.Authenticated) {
                                navController.navigate("account") // Navega para a tela da conta
                            } else {
                                navController.navigate("login") // Navega para a tela de login
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Espaço antes da mensagem de boas-vindas

            // Mensagem de Boas-Vindas
            if (authState.value is AuthState.Authenticated && currentUser != null) {
                val displayName = currentUser.displayName
                if (!displayName.isNullOrBlank()) {
                    Text(
                        text = "Bem-vindo, $displayName!",
                        fontSize = 24.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Fallback se o displayName for nulo ou vazio, mas o usuário estiver autenticado
                    Text(
                        text = "Bem-vindo!",
                        fontSize = 24.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}