package com.example.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val authState: AuthState = AuthState.Idle,
    val userProfile: UserProfileData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAuthDialog: Boolean = false,
    val isSignUpMode: Boolean = false
)

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkInitialAuth()
        listenToAuthChanges()
    }

    private fun checkInitialAuth() {
        val user = authManager.currentUser
        if (user != null) {
            viewModelScope.launch {
                val profile = authManager.syncUserProfile(user)
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile)
                    )
                }
            }
        } else {
            val localProfile = authManager.getLocalUserProfile()
            if (localProfile != null) {
                _uiState.update {
                    it.copy(
                        userProfile = localProfile,
                        authState = AuthState.Authenticated(localProfile)
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        userProfile = null,
                        authState = AuthState.Unauthenticated()
                    )
                }
            }
        }
    }

    private fun listenToAuthChanges() {
        viewModelScope.launch {
            authManager.authStateFlow.collect { user ->
                if (user != null) {
                    val profile = authManager.syncUserProfile(user)
                    _uiState.update {
                        it.copy(
                            userProfile = profile,
                            authState = AuthState.Authenticated(profile)
                        )
                    }
                } else {
                    val localProfile = authManager.getLocalUserProfile()
                    if (localProfile != null) {
                        _uiState.update {
                            it.copy(
                                userProfile = localProfile,
                                authState = AuthState.Authenticated(localProfile)
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                userProfile = null,
                                authState = AuthState.Unauthenticated()
                            )
                        }
                    }
                }
            }
        }
    }

    fun openAuthDialog(isSignUp: Boolean = false) {
        _uiState.update {
            it.copy(
                showAuthDialog = true,
                isSignUpMode = isSignUp,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun closeAuthDialog() {
        _uiState.update {
            it.copy(
                showAuthDialog = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun toggleSignUpMode(isSignUp: Boolean) {
        _uiState.update { it.copy(isSignUpMode = isSignUp, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authManager.signInWithEmail(email, pass)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile),
                        showAuthDialog = false,
                        successMessage = "Logged in successfully!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Login failed"
                    )
                }
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }
        if (pass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authManager.signUpWithEmail(email, pass, name)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile),
                        showAuthDialog = false,
                        successMessage = "Account created successfully!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Sign up failed"
                    )
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authManager.signInWithGoogle(context)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile),
                        showAuthDialog = false,
                        successMessage = "Signed in with Google!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Google sign in failed"
                    )
                }
            }
        }
    }

    fun linkGoogleAccount(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authManager.linkGoogleAccount(context)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile),
                        successMessage = "Google account linked successfully! You can now sign in with either Email or Google."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to link Google account"
                    )
                }
            }
        }
    }

    fun unlinkGoogleAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authManager.unlinkGoogleAccount()
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        authState = AuthState.Authenticated(profile),
                        successMessage = "Google account unlinked."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to unlink account"
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authManager.signOut()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userProfile = null,
                    authState = AuthState.Unauthenticated(),
                    successMessage = "Signed out"
                )
            }
        }
    }
}

class AuthViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
