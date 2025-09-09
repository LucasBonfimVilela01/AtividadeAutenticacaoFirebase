package com.example.atividadefirebase.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.atividadefirebase.AuthState
import com.example.atividadefirebase.AuthViewModel
import com.example.atividadefirebase.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    val currentUser = authViewModel.getCurrentUser()

    var displayName by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }

    // Estados para o formulário de novo produto
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var isLoadingAddProduct by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogText by remember { mutableStateOf("") }
    var dialogConfirmAction by remember { mutableStateOf<() -> Unit>({}) }
    var showDialogDismissButton by remember { mutableStateOf(false) }

    var isLoadingSave by remember { mutableStateOf(false) }
    var isLoadingDelete by remember { mutableStateOf(false) }


    LaunchedEffect(authState) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (authState is AuthState.Unauthenticated && currentRoute != "login" && currentRoute != "signup") {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Usuário não autenticado. Redirecionando...")
        }
        return
    }

    LaunchedEffect(currentUser?.displayName, currentUser?.email) {
        displayName = currentUser?.displayName ?: ""
        email = currentUser?.email ?: ""
    }

    // Adicionar um ScrollState para permitir rolagem se o conteúdo exceder a tela
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), // Adicionar rolagem vertical
        horizontalAlignment = Alignment.CenterHorizontally,
        // verticalArrangement = Arrangement.Center // Remover para permitir que o conteúdo comece do topo
    ) {
        // Seção de Informações da Conta
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Informações da Conta", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Ícone do Perfil",
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nome de exibição") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { /* Não editável */ },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoadingSave = true
                authViewModel.updateUserProfile(
                    newDisplayName = displayName,
                    onSuccess = {
                        isLoadingSave = false
                        dialogTitle = "Sucesso"
                        dialogText = "Perfil atualizado com sucesso!"
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    },
                    onError = { errorMsg ->
                        isLoadingSave = false
                        dialogTitle = "Erro"
                        dialogText = "Falha ao atualizar o perfil: $errorMsg"
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct
        ) {
            if (isLoadingSave) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Salvar alterações")
            }
        }
        Spacer(modifier = Modifier.height(24.dp)) // Aumentar o espaço antes da próxima seção

        // --- Seção de Cadastro de Novo Produto ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Cadastrar Novo Produto", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Nome produto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productDescription,
            onValueChange = { productDescription = it },
            label = { Text("Descrição produto") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoadingAddProduct = true
                // Usar o displayName atualizado do estado, ou o do currentUser se preferir
                val currentUserName = displayName.ifBlank { currentUser?.displayName }

                authViewModel.addProductToDatabase(
                    productName = productName,
                    productDescription = productDescription,
                    userName = currentUserName, // Passa o nome de exibição do usuário
                    onSuccess = {
                        isLoadingAddProduct = false
                        productName = "" // Limpar campos
                        productDescription = ""
                        dialogTitle = "Sucesso"
                        dialogText = "Produto adicionado com sucesso!"
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    },
                    onError = { errorMsg ->
                        isLoadingAddProduct = false
                        dialogTitle = "Erro ao Adicionar Produto"
                        dialogText = errorMsg
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct && productName.isNotBlank() && productDescription.isNotBlank()
        ) {
            if (isLoadingAddProduct) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Adicionar produto")
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Aumentar o espaço

        //Botões de Sair e Excluir Conta
        Button(
            onClick = { authViewModel.signout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct
        ) {
            Text("Sair")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                dialogTitle = "Confirmar exclusão"
                dialogText = "Tem certeza de que deseja excluir sua conta? Esta ação não pode ser desfeita."
                showDialogDismissButton = true
                dialogConfirmAction = {
                    showDialog = false
                    isLoadingDelete = true
                    authViewModel.deleteAccount(
                        onSuccess = {
                            isLoadingDelete = false
                        },
                        onError = { errorMsg ->
                            isLoadingDelete = false
                            dialogTitle = "Falha na exclusão"
                            dialogText = "Falha ao excluir a conta: $errorMsg"
                            showDialogDismissButton = false
                            dialogConfirmAction = { showDialog = false }
                            showDialog = true
                        }
                    )
                }
                showDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct
        ) {
            if (isLoadingDelete) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Excluir conta")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoadingSave && !isLoadingDelete && !isLoadingAddProduct) showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                Button(
                    onClick = dialogConfirmAction,
                    enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct
                ) {
                    Text(if (dialogTitle == "Confirmar exclusão") "Excluir" else "OK")
                }
            },
            dismissButton = {
                if (showDialogDismissButton) {
                    TextButton(
                        onClick = { showDialog = false },
                        enabled = !isLoadingSave && !isLoadingDelete && !isLoadingAddProduct
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}
