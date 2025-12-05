package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Auth
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult
import my.drivebit.utils.Validator

class EmailLoginViewModel(
    private val auth: Auth,
    private val emailValidator: Validator,
    private val emailInputValidator: InputValidator,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AuthFormViewModel {
    private val viewModelScope = coroutineScope

    override val pageTitle: String = "Войти или создать аккаунт"
    override val fieldLabel: String = "Email"
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

            val result =
                runCatching {
                    auth.createOtp(input)
                }

            result.fold(
                onSuccess = { response ->
                    _state.update { AuthFormState.Success(response.sessionId) }
                },
                onFailure = { throwable ->
                    val errorMessage = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                    _state.update { AuthFormState.Error(errorMessage) }
                },
            )
        }
    }

    override fun clearError() {
        _state.update { AuthFormState.Idle }
    }
}
