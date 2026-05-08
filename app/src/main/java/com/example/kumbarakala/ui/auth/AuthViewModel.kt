package com.example.kumbarakala.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kumbarakala.data.ProfileRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class AuthViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    companion object {
        private const val AUTH_REQUEST_TIMEOUT_MS = 15000L
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        val newMode = if (_uiState.value.mode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
        _uiState.update { 
            it.copy(
                mode = newMode,
                error = null,
                nameError = null,
                emailError = null,
                phoneError = null,
                passwordError = null
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val state = _uiState.value
        var isValid = true

        // Validation
        if (state.mode == AuthMode.SIGNUP && state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            isValid = false
        }

        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (!isValid) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                withTimeout(AUTH_REQUEST_TIMEOUT_MS) {
                    if (state.mode == AuthMode.SIGNUP) {
                        profileRepository.signUp(
                            email = state.email,
                            password = state.password,
                            name = state.name,
                            phone = state.phone.takeIf { it.isNotBlank() }
                        )
                    } else {
                        profileRepository.login(
                            email = state.email,
                            password = state.password
                        )
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                if (e is TimeoutCancellationException && profileRepository.isAuthenticated()) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = mapAuthError(e, state.mode)
                        )
                    }
                }
            }
        }
    }

    private fun mapAuthError(error: Exception, mode: AuthMode): String {
        return when (error) {
            is FirebaseNetworkException -> "No internet connection. Please check your network and try again."
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered. Please log in."
                "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled in Firebase Console."
                "ERROR_USER_NOT_FOUND",
                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_LOGIN_CREDENTIALS" -> {
                    if (mode == AuthMode.LOGIN) {
                        "Incorrect email or password."
                    } else {
                        "Invalid credentials. Please check your email and password."
                    }
                }
                "ERROR_INTERNAL_ERROR" -> "Firebase internal error. Check Firebase Console setup: enable Email/Password sign-in and ensure API key restrictions are not blocking this app."
                else -> "Authentication failed (${error.errorCode}): ${error.message ?: "Unknown Firebase error"}"
            }
            is FirebaseException -> error.message ?: "Firebase request failed. Please verify Firebase project setup."
            is TimeoutCancellationException ->
                "Request timed out. Please check internet and Firebase setup, then try again."
            else -> error.message ?: "Authentication failed. Please try again."
        }
    }
}
