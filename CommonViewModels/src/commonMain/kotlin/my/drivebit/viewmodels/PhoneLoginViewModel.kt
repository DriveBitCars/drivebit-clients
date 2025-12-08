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

class PhoneLoginViewModel(
    private val createOtpRepository: CreateOtpRepository,
    private val phoneValidator: Validator,
    private val phoneInputValidator: InputValidator,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AuthFormViewModel {
    private val viewModelScope = coroutineScope

    override val pageTitle: String = "Войти или создать аккаунт"
    override val fieldLabel: String = "Телефон"
    override val inputType: InputFieldType = InputFieldType.Phone
    override val autocomplete: String = "tel"
    override val inputName: String = "tel"
    override val initialInputValue: String = "+7"
    override val primaryButtonText: String = "Продолжить"
    override val secondaryButtonText: String = "Войти по email"
    override val secondaryButtonNavigationPath: String = "/login-by-mail"

    private val _state = MutableStateFlow<AuthFormState>(AuthFormState.Idle)
    override val state: StateFlow<AuthFormState> = _state.asStateFlow()

    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Error("Введите номер телефона"))
    override val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    override fun formatInput(input: String): String = phoneValidator.validate(input)

    override fun validateInput(input: String) {
        if (_state.value is AuthFormState.Error) {
            clearError()
        }
        val validationResult = phoneInputValidator.isValid(input)
        _validationState.update {
            when (validationResult) {
                is ValidationResult.Valid -> ValidationState.Valid
                is ValidationResult.Invalid -> ValidationState.Error(validationResult.errorMessage)
            }
        }
    }

    override fun submit(input: String) {
        val validationResult = phoneInputValidator.isValid(input)
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
