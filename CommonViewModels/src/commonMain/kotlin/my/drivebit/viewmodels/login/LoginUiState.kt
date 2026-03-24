package my.drivebit.viewmodels.login

import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.InputFieldType

data class LoginUiState(
    val input: String,
    val termsAccepted: Boolean,
    val requiresTermsConsent: Boolean,
    val authState: AuthFormState,
    val pageTitle: String,
    val fieldLabel: String,
    val inputType: InputFieldType,
    val autocomplete: String,
    val inputName: String,
    val primaryButtonText: String,
    val secondaryButtonText: String,
    val secondaryButtonNavigationPath: String,
)
