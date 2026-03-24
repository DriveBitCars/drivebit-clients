package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import my.drivebit.viewmodels.login.LoginIntent
import my.drivebit.viewmodels.login.LoginMviViewModelImpl
import my.drivebit.viewmodels.login.initialInputForLogin

internal class LoginAuthFormAdapter(
    private val impl: LoginMviViewModelImpl,
) : AuthFormViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val pageTitle: String
        get() = impl.uiState.value.pageTitle

    override val fieldLabel: String
        get() = impl.uiState.value.fieldLabel

    override val inputType: InputFieldType
        get() = impl.uiState.value.inputType

    override val autocomplete: String
        get() = impl.uiState.value.autocomplete

    override val inputName: String
        get() = impl.uiState.value.inputName

    override val initialInputValue: String
        get() = initialInputForLogin(impl.screenKind)

    override val primaryButtonText: String
        get() = impl.uiState.value.primaryButtonText

    override val secondaryButtonText: String
        get() = impl.uiState.value.secondaryButtonText

    override val secondaryButtonNavigationPath: String
        get() = impl.uiState.value.secondaryButtonNavigationPath

    override val state: StateFlow<AuthFormState> =
        impl.uiState
            .map { it.authState }
            .stateIn(scope, SharingStarted.Eagerly, AuthFormState.Idle)

    override val validationState: StateFlow<ValidationState> =
        impl.validatorViewModel.validationState

    override val requiresTermsConsent: Boolean
        get() = impl.uiState.value.requiresTermsConsent

    override val termsConsentAccepted: StateFlow<Boolean> =
        impl.uiState
            .map { it.termsAccepted }
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                impl.uiState.value.termsAccepted,
            )

    override fun formatInput(input: String): String = impl.formatRawInput(input)

    override fun validateInput(input: String) {
        impl.handleIntent(LoginIntent.InputChanged(input))
    }

    override fun submit(input: String) {
        impl.handleIntent(LoginIntent.Submit)
    }

    override fun clearError() {
        impl.handleIntent(LoginIntent.ClearAuthError)
    }

    override fun setTermsConsent(accepted: Boolean) {
        impl.handleIntent(LoginIntent.TermsChanged(accepted))
    }
}
