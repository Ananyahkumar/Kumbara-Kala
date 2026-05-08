package com.example.kumbarakala.ui.auth

data class User(
    val name: String,
    val email: String,
    val phone: String,
    val title: String = "Master Potter",
    val bio: String = ""
)

enum class AuthMode {
    LOGIN,
    SIGNUP
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Form fields
    val name: String = "",
    val nameError: String? = null,
    
    val email: String = "",
    val emailError: String? = null,
    
    val phone: String = "",
    val phoneError: String? = null,
    
    val password: String = "",
    val passwordError: String? = null,
    
    val isPasswordVisible: Boolean = false,
    
    val isSuccess: Boolean = false
)
