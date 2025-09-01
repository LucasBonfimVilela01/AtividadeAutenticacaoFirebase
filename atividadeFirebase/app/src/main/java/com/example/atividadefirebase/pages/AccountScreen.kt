package com.example.atividadefirebase.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.atividadefirebase.AuthState
import com.example.atividadefirebase.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    // Obtém currentUser do ViewModel ou o observa se você adicionar um LiveData para ele
    val currentUser = authViewModel.getCurrentUser() // Usando o novo auxiliar

    // Inicializa com as informações do usuário atual ou padrões se nulo (embora protegido por LaunchedEffect)
    var displayName by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }


    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") } // Para melhor gerenciamento de diálogo
    var dialogText by remember { mutableStateOf("") }
    var dialogConfirmAction by remember { mutableStateOf<() -> Unit>({}) }
    var showDialogDismissButton by remember { mutableStateOf(false) }

    var isLoadingSave by remember { mutableStateOf(false) } // Carregamento específico para salvar
    var isLoadingDelete by remember { mutableStateOf(false) } // Carregamento específico para excluir


    LaunchedEffect(authState) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (authState is AuthState.Unauthenticated && currentRoute != "login" && currentRoute != "signup") {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Isso lida com o caso em que o composable é carregado antes que authState seja Não autenticado,
    // ou se currentUser se tornar nulo devido a alguma outra condição de corrida.
    if (currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Usuário não autenticado. Redirecionando...")
            // O LaunchedEffect acima deve lidar com a navegação.
            // Você pode adicionar um CircularProgressIndicator aqui se quiser.
        }
        return // Para de renderizar o resto da UI se não houver usuário
    }

    // Atualiza o estado local se currentUser mudar (por exemplo, após a atualização do perfil e nova busca)
    // Isso é importante se o AuthViewModel não acionar diretamente uma recomposição desta tela
    // com novos dados do usuário.
    LaunchedEffect(currentUser?.displayName, currentUser?.email) {
        displayName = currentUser?.displayName ?: ""
        email = currentUser?.email ?: ""
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                modifier = Modifier.size(36.dp) // Ajuste o tamanho conforme necessário
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
            value = email, // Email é normalmente de auth.currentUser.email
            onValueChange = { /* Geralmente não é alterado aqui sem verificação */ },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true, // As atualizações de email geralmente exigem um fluxo de verificação separado
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoadingSave = true
                authViewModel.updateUserProfile( // Use a função ViewModel
                    newDisplayName = displayName,
                    onSuccess = {
                        isLoadingSave = false
                        // Opcionalmente, atualize os detalhes do currentUser se não forem atualizados automaticamente
                        // authViewModel.refreshCurrentUser() // Você pode precisar de tal função
                        dialogTitle = "Sucesso" // Traduzido
                        dialogText = "Perfil atualizado com sucesso!"
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    },
                    onError = { errorMsg ->
                        isLoadingSave = false
                        dialogTitle = "Erro" // Traduzido
                        dialogText = "Falha ao atualizar o perfil: $errorMsg"
                        showDialogDismissButton = false
                        dialogConfirmAction = { showDialog = false }
                        showDialog = true
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSave && !isLoadingDelete
        ) {
            if (isLoadingSave) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Salvar alterações")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { authViewModel.signout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Sair")
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced space for better grouping

        Button(
            onClick = {
                dialogTitle = "Confirmar exclusão"
                dialogText = "Tem certeza de que deseja excluir sua conta? Esta ação não pode ser desfeita."
                showDialogDismissButton = true
                dialogConfirmAction = {
                    showDialog = false // Fecha o diálogo de confirmação
                    isLoadingDelete = true
                    authViewModel.deleteAccount(
                        onSuccess = {
                            isLoadingDelete = false
                            // A navegação é tratada pelo LaunchedEffect observando authState
                        },
                        onError = { errorMsg ->
                            isLoadingDelete = false
                            dialogTitle = "Falha na exclusão"
                            dialogText = "Falha ao excluir a conta: $errorMsg"
                            showDialogDismissButton = false
                            dialogConfirmAction = { showDialog = false } // Ação para diálogo de erro
                            showDialog = true // Mostra diálogo de erro
                        }
                    )
                }
                showDialog = true // Mostra diálogo de confirmação
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoadingSave && !isLoadingDelete
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
            onDismissRequest = { if (!isLoadingSave && !isLoadingDelete) showDialog = false }, // Impede o fechamento durante o carregamento
            title = { Text(dialogTitle) },
            text = { Text(dialogText) },
            confirmButton = {
                Button(
                    onClick = dialogConfirmAction,
                    enabled = !isLoadingSave && !isLoadingDelete
                ) {
                    Text(if (dialogTitle == "Confirmar exclusão") "Excluir" else "OK")
                }
            },
            dismissButton = {
                if (showDialogDismissButton) {
                    TextButton(
                        onClick = { showDialog = false },
                        enabled = !isLoadingSave && !isLoadingDelete
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}
