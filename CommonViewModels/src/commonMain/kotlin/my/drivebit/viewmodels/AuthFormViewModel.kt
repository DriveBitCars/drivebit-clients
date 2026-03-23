package my.drivebit.viewmodels

import kotlinx.coroutines.flow.StateFlow

sealed interface ValidationState {
    data object Valid : ValidationState

    data class Error(
        val message: String,
    ) : ValidationState
}

enum class InputFieldType {
    Phone,
    Email,
}

sealed interface AuthFormViewModel {
    val pageTitle: String
    val fieldLabel: String
    val inputType: InputFieldType
    val autocomplete: String
    val inputName: String
    val initialInputValue: String
    val primaryButtonText: String
    val secondaryButtonText: String
    val secondaryButtonNavigationPath: String
    val state: StateFlow<AuthFormState>
    val validationState: StateFlow<ValidationState>
    val requiresTermsConsent: Boolean
    val termsConsentAccepted: StateFlow<Boolean>

    fun formatInput(input: String): String

    fun validateInput(input: String)

    fun submit(input: String)

    fun clearError()

    fun setTermsConsent(accepted: Boolean)
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
