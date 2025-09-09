package com.example.atividadefirebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val addedBy: String = "",
    val userId: String = ""
)

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

    private val database = Firebase.database.reference.child("products")

    // Usando StateFlow para a lista de produtos (preferível em Compose moderno)
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val productsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = mutableListOf<Product>()
            snapshot.children.forEach { productSnapshot ->
                // Mapeia o DataSnapshot para o objeto Product
                val product = productSnapshot.getValue(Product::class.java)?.copy(id = productSnapshot.key ?: "")
                product?.let { productList.add(it) }
            }
            _products.value = productList
        }

        override fun onCancelled(error: DatabaseError) {
            _products.value = emptyList() // Limpa em caso de erro
        }
    }

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        database.addValueEventListener(productsListener)
    }

    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(productsListener)
    }

    fun addProductToDatabase(
        productName: String,
        productDescription: String,
        userName: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (productName.isBlank() || productDescription.isBlank()) {
            Log.e("AddProduct", "Nome ou descrição do produto em branco.")
            onError("Nome e descrição do produto não podem estar vazios.")
            return
        }

        val userIdAuth = auth.currentUser?.uid
        if (userIdAuth == null) {
            Log.e("AddProduct", "Usuário não autenticado para adicionar produto.")
            onError("Usuário não autenticado.")
            return
        }

        val productId = database.child("products").push().key
        val specificProductsRef = Firebase.database.reference.child("products")


        if (productId == null) {
            Log.e("AddProduct", "Falha ao gerar productId.")
            onError("Não foi possível gerar um ID para o produto.")
            return
        }

        val productData = hashMapOf(
            "name" to productName,
            "description" to productDescription,
            "addedBy" to (userName ?: "Usuário Desconhecido"),
            "userId" to userIdAuth // ID do usuário que adicionou
        )

        Log.d("AddProduct", "Tentando salvar produto no Firebase: $productId -> $productData")

        specificProductsRef.child(productId).setValue(productData)
            .addOnSuccessListener {
                Log.i("AddProduct", "SUCESSO ao salvar produto $productId no Firebase.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("AddProduct", "FALHA ao salvar produto $productId no Firebase: ${e.message}", e)
                onError(e.message ?: "Falha ao adicionar produto.")
            }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}