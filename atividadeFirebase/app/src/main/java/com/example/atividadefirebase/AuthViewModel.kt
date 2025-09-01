package com.example.atividadefirebase

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isUpdatingProfile = MutableLiveData<Boolean>(false)
    val isUpdatingProfile: LiveData<Boolean> = _isUpdatingProfile

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() { // Tornou-se privado, pois é um auxiliar interno
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) { // Use isBlank para melhor validação
            _authState.value = AuthState.Error("O email ou a senha não podem estar vazios")
            return // Retorne mais cedo
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Falha ao fazer o login")
                }
            }
    }

    // Função de inscrição atualizada
    fun signup(displayName: String, email: String, password: String) {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Todos os campos devem ser preenchidos")
            return // Retorne mais cedo
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuário criado, agora atualize o nome de exibição dele
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                // Profile update failed, but account was created.
                                // You might want to inform the user or log this.
                                _authState.value = AuthState.Error(
                                    profileTask.exception?.message ?: "Conta criada, mas falha ao definir o nome de exibição."
                                )
                                // Opcionalmente, ainda trate como Autenticado se o nome de exibição não for crítico para o login imediato
                                // _authState.value = AuthState.Autenticado
                            }
                        }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Falha ao criar a conta")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUser(): FirebaseUser? { // Auxiliar para obter o usuário atual, se necessário em outro lugar
        return auth.currentUser
    }


    fun updateUserProfile(
        newDisplayName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isUpdatingProfile.value = true
        val user = auth.currentUser
        user?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build()

            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    _isUpdatingProfile.value = false
                    if (task.isSuccessful) {
                        // Atualize o authState local, se necessário, ou deixe a UI buscar/recompor
                        // Por exemplo, se você armazena detalhes do usuário no authState:
                        // _authState.value = AuthState.Autenticado // (ou um estado mais específico com informações atualizadas do usuário)
                        onSuccess()
                    } else {
                        onError(task.exception?.message ?: "Falha ao atualizar o nome de exibição.")
                    }
                }
        } ?: run {
            _isUpdatingProfile.value = false
            onError("Usuário não encontrado.")
        }
    }


    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _authState.value = AuthState.Loading
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated
                    onSuccess()
                } else {
                    if (task.exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                        _authState.value =
                            AuthState.Error("Por favor, faça login novamente para excluir sua conta.")
                        onError("Login recente necessário. Por favor, saia e faça login novamente.")
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.message ?: "Falha ao excluir a conta.")
                        onError(task.exception?.message ?: "Erro desconhecido ao excluir a conta.")
                    }
                }
            } ?: run {
            _authState.value = AuthState.Error("Usuário não encontrado.")
            onError("Usuário não encontrado.")
        }
    }
}

// AuthState remains the same
sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}