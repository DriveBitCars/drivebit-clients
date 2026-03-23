package my.drivebit.viewmodels

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

class EmailLoginViewModel(
    private val createOtpRepository: CreateOtpRepository,
    private val emailValidator: Validator,
    private val emailInputValidator: InputValidator,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val requireTermsConsent: Boolean = true,
) : AuthFormViewModel {
    private val viewModelScope = coroutineScope

    override val requiresTermsConsent: Boolean = requireTermsConsent

    private val _termsConsentAccepted = MutableStateFlow(!requireTermsConsent)
    override val termsConsentAccepted: StateFlow<Boolean> = _termsConsentAccepted.asStateFlow()

    override fun setTermsConsent(accepted: Boolean) {
        if (requireTermsConsent) {
            _termsConsentAccepted.value = accepted
        }
    }

    override val pageTitle: String = "Войти или создать аккаунт"
    override val fieldLabel: String = "Email"
    override val inputType: InputFieldType = InputFieldType.Email
    override val autocomplete: String = "email"
    override val inputName: String = "email"
    override val initialInputValue: String = ""
    override val primaryButtonText: String = "Продолжить"
    override val secondaryButtonText: String = "Войти по телефону"
    override val secondaryButtonNavigationPath: String = "/login-by-phone"

    private val _state = MutableStateFlow<AuthFormState>(AuthFormState.Idle)
    override val state: StateFlow<AuthFormState> = _state.asStateFlow()

    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Error("Введите email"))
    override val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    override fun formatInput(input: String): String = emailValidator.validate(input)

    override fun validateInput(input: String) {
        if (_state.value is AuthFormState.Error) {
            clearError()
        }
        val validationResult = emailInputValidator.isValid(input)
        _validationState.update {
            when (validationResult) {
                is ValidationResult.Valid -> ValidationState.Valid
                is ValidationResult.Invalid -> ValidationState.Error(validationResult.errorMessage)
            }
        }
    }

    override fun submit(input: String) {
        val validationResult = emailInputValidator.isValid(input)
        if (validationResult is ValidationResult.Invalid) {
            _state.update { AuthFormState.Error(validationResult.errorMessage) }
            return
        }

        viewModelScope.launch {
            _state.update { AuthFormState.Loading }

            when (val result = createOtpRepository.createOtp(input)) {
                is ResultOtp.Success -> {
                    _state.update { AuthFormState.Success(result.sessionId) }
                }
                is ResultOtp.Error -> {
                    _state.update { AuthFormState.Error(result.message) }
                }
            }
        }
    }

    override fun clearError() {
        _state.update { AuthFormState.Idle }
    }
}
