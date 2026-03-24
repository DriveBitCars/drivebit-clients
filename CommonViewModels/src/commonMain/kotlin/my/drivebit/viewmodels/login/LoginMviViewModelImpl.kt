package my.drivebit.viewmodels.login

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.ResultOtp
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult
import my.drivebit.utils.Validator
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.InputFieldType
import my.drivebit.viewmodels.LoginAuthFormAdapter
import my.drivebit.viewmodels.ValidatorViewModel

enum class LoginScreenKind {
    Phone,
    Email,
}

class LoginMviViewModelImpl(
    private val kind: LoginScreenKind,
    private val createOtpRepository: CreateOtpRepository,
    private val phoneValidator: Validator,
    private val phoneInputValidator: InputValidator,
    private val emailValidator: Validator,
    private val emailInputValidator: InputValidator,
    private val loginDraftStorage: LoginDraftStorage,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val requireTermsConsent: Boolean = true,
) : LoginMviViewModel {
    internal val screenKind: LoginScreenKind
        get() = kind

    private val viewModelScope = coroutineScope

    private val validatorViewModelInternal: ValidatorViewModel =
        when (kind) {
            LoginScreenKind.Phone ->
                ValidatorViewModel(
                    validator = phoneValidator,
                    inputValidator = phoneInputValidator,
                    initialErrorMessage = "Введите номер телефона",
                )
            LoginScreenKind.Email ->
                ValidatorViewModel(
                    validator = emailValidator,
                    inputValidator = emailInputValidator,
                    initialErrorMessage = "Введите email",
                )
        }

    override val validatorViewModel: ValidatorViewModel
        get() = validatorViewModelInternal

    private val _uiState =
        MutableStateFlow(
            buildInitialUiState(
                kind = kind,
                requireTermsConsent = requireTermsConsent,
                input = initialInputForLogin(kind),
            ),
        )

    override val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val authFormAdapter by lazy { LoginAuthFormAdapter(this) }

    override fun asAuthFormViewModel(): AuthFormViewModel = authFormAdapter

    override fun clearDraftStorage() {
        loginDraftStorage.clear()
    }

    internal fun formatRawInput(raw: String): String =
        when (kind) {
            LoginScreenKind.Phone -> phoneValidator.validate(raw)
            LoginScreenKind.Email -> emailValidator.validate(raw)
        }

    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.InputChanged -> applyInputChanged(intent.raw)
            is LoginIntent.TermsChanged -> applyTermsChanged(intent.accepted)
            LoginIntent.Submit -> submitCurrentInput()
            LoginIntent.ClearAuthError -> clearAuthError()
            LoginIntent.RestoreDraft -> restoreDraft()
        }
    }

    private fun applyInputChanged(raw: String) {
        if (_uiState.value.authState is AuthFormState.Error) {
            clearAuthError()
        }
        val formatted = formatRawInput(raw)
        validatorViewModelInternal.validateInput(formatted)
        _uiState.update { it.copy(input = formatted) }
        persistDraft()
    }

    private fun applyTermsChanged(accepted: Boolean) {
        if (!requireTermsConsent) return
        _uiState.update { it.copy(termsAccepted = accepted) }
        persistDraft()
    }

    private fun submitCurrentInput() {
        val input = _uiState.value.input
        val validationResult =
            when (kind) {
                LoginScreenKind.Phone -> phoneInputValidator.isValid(input)
                LoginScreenKind.Email -> emailInputValidator.isValid(input)
            }
        if (validationResult is ValidationResult.Invalid) {
            _uiState.update { it.copy(authState = AuthFormState.Error(validationResult.errorMessage)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(authState = AuthFormState.Loading) }

            when (val result = createOtpRepository.createOtp(input)) {
                is ResultOtp.Success -> {
                    _uiState.update {
                        it.copy(authState = AuthFormState.Success(result.sessionId))
                    }
                }
                is ResultOtp.Error -> {
                    val message =
                        result.message.ifBlank { "Произошла ошибка" }
                    _uiState.update {
                        it.copy(authState = AuthFormState.Error(message))
                    }
                }
            }
        }
    }

    private fun clearAuthError() {
        _uiState.update { it.copy(authState = AuthFormState.Idle) }
    }

    private fun restoreDraft() {
        val draft = loginDraftStorage.load() ?: return
        if (!draftIdentifierMatchesScreen(draft.identifier, kind)) return
        val formatted = formatRawInput(draft.identifier)
        validatorViewModelInternal.validateInput(formatted)
        _uiState.update {
            it.copy(
                input = formatted,
                termsAccepted = if (requireTermsConsent) draft.termsAccepted else it.termsAccepted,
            )
        }
    }

    private fun persistDraft() {
        val s = _uiState.value
        loginDraftStorage.save(
            LoginDraft(
                identifier = s.input,
                termsAccepted = s.termsAccepted,
            ),
        )
    }
}

private fun buildInitialUiState(
    kind: LoginScreenKind,
    requireTermsConsent: Boolean,
    input: String,
): LoginUiState =
    when (kind) {
        LoginScreenKind.Phone ->
            LoginUiState(
                input = input,
                termsAccepted = !requireTermsConsent,
                requiresTermsConsent = requireTermsConsent,
                authState = AuthFormState.Idle,
                pageTitle = "Войти или создать аккаунт",
                fieldLabel = "Телефон",
                inputType = InputFieldType.Phone,
                autocomplete = "tel",
                inputName = "tel",
                primaryButtonText = "Продолжить",
                secondaryButtonText = "Войти по email",
                secondaryButtonNavigationPath = "/login-by-mail",
            )
        LoginScreenKind.Email ->
            LoginUiState(
                input = input,
                termsAccepted = !requireTermsConsent,
                requiresTermsConsent = requireTermsConsent,
                authState = AuthFormState.Idle,
                pageTitle = "Войти или создать аккаунт",
                fieldLabel = "Email",
                inputType = InputFieldType.Email,
                autocomplete = "email",
                inputName = "email",
                primaryButtonText = "Продолжить",
                secondaryButtonText = "Войти по телефону",
                secondaryButtonNavigationPath = "/login-by-phone",
            )
    }

internal fun initialInputForLogin(kind: LoginScreenKind): String =
    when (kind) {
        LoginScreenKind.Phone -> "+7"
        LoginScreenKind.Email -> ""
    }

internal fun draftIdentifierMatchesScreen(
    identifier: String,
    kind: LoginScreenKind,
): Boolean {
    val trimmed = identifier.trim()
    if (trimmed.isEmpty()) return false
    return when (kind) {
        LoginScreenKind.Phone -> !trimmed.contains('@')
        LoginScreenKind.Email -> !trimmed.startsWith('+')
    }
}
