package my.drivebit.viewmodels

import kotlinx.coroutines.flow.StateFlow

sealed interface ValidationState {
    data object Valid : ValidationState

    data class Error(
        val message: String,
    ) : ValidationState
}

sealed interface AuthFormViewModel {
    val pageTitle: String
    val fieldLabel: String
    val primaryButtonText: String
    val secondaryButtonText: String
    val secondaryButtonNavigationPath: String
    val state: StateFlow<AuthFormState>
    val validationState: StateFlow<ValidationState>

    fun formatInput(input: String): String

    fun validateInput(input: String)

    fun submit(input: String)

    fun clearError()
}

sealed class AuthFormState {
    object Idle : AuthFormState()

    object Loading : AuthFormState()

    data class Success(
        val identifier: String,
    ) : AuthFormState()

    data class Error(
        val message: String,
    ) : AuthFormState()
}
