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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.atividadefirebase.Product

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavHostController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val currentUser: FirebaseUser? = authViewModel.getCurrentUser()

    val productsList by authViewModel.products.collectAsState()


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
            Spacer(modifier = Modifier.height(16.dp)) // Espaço antes da lista de produtos

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Produtos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                IconButton(onClick = { authViewModel.refreshProducts() }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Atualizar produtos",
                        tint = Color.DarkGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Espaço antes da lista de produtos
            // --- Lista de Produtos ---
            if (productsList.isEmpty() && authState.value is AuthState.Authenticated) {
                Text(
                    "Nenhum produto cadastrado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (productsList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Faz a LazyColumn ocupar o espaço restante
                        .padding(horizontal = 16.dp), // Padding para a lista
                    contentPadding = PaddingValues(bottom = 16.dp) // Padding no final da lista
                ) {
                    items(productsList, key = { product -> product.id }) { product ->
                        ProductItem(product = product)
                        Spacer(modifier = Modifier.height(8.dp)) // Espaço entre os itens
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Produto criado por: ${product.addedBy}",
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}