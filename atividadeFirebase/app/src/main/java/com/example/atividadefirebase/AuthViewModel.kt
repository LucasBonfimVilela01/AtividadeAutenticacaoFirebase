package com.example.atividadefirebase

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.ui.input.key.key
import androidx.core.view.children
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope // Import viewModelScope
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
import kotlinx.coroutines.launch // Import launch

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

    // --- Database Reference for Products ---
    // Moved here for clarity and to avoid re-declaration
    private val productsDatabaseRef = Firebase.database.reference.child("products")

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val productsListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = mutableListOf<Product>()
            snapshot.children.forEach { productSnapshot ->
                val product = productSnapshot.getValue(Product::class.java)?.copy(id = productSnapshot.key ?: "")
                product?.let { productList.add(it) }
            }
            _products.value = productList
            Log.d("AuthViewModel", "Products updated: ${productList.size} items")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("AuthViewModel", "Error fetching products: ${error.message}", error.toException())
            _products.value = emptyList() // Clear on error
        }
    }

    init {
        checkAuthStatus()
        fetchProducts() // Initial fetch when ViewModel is created
    }

    private fun checkAuthStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("O email ou a senha não podem estar vazios")
            return
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

    fun signup(displayName: String, email: String, password: String) {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Todos os campos devem ser preenchidos")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error(
                                    profileTask.exception?.message ?: "Conta criada, mas falha ao definir o nome de exibição."
                                )
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

    fun getCurrentUser(): FirebaseUser? {
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

    private fun fetchProducts() {
        productsDatabaseRef.removeEventListener(productsListener)
        productsDatabaseRef.addValueEventListener(productsListener)
        Log.d("AuthViewModel", "fetchProducts called, listener attached.")
    }

    fun refreshProducts() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "refreshProducts() na UI.")
            productsDatabaseRef.removeEventListener(productsListener)
            productsDatabaseRef.addValueEventListener(productsListener)
             Log.d("AuthViewModel", "ValueEventListener re-attached/ensured for products.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        productsDatabaseRef.removeEventListener(productsListener)
        Log.d("AuthViewModel", "onCleared called, listener removed.")
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

        val productId = productsDatabaseRef.push().key

        if (productId == null) {
            Log.e("AddProduct", "Falha ao gerar productId.")
            onError("Não foi possível gerar um ID para o produto.")
            return
        }

        val productData = hashMapOf(
            "name" to productName,
            "description" to productDescription,
            "addedBy" to (userName ?: "Usuário Desconhecido"),
            "userId" to userIdAuth
        )

        Log.d("AddProduct", "Tentando salvar produto no Firebase: $productId -> $productData")

        productsDatabaseRef.child(productId).setValue(productData)
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
