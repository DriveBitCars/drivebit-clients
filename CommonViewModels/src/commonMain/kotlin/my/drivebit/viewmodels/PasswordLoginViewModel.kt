package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.repositories.PasswordLoginRepository
import my.drivebit.repositories.PasswordLoginResult

data class PasswordLoginUiState(
    val login: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val authState: AuthFormState = AuthFormState.Idle,
)

class PasswordLoginViewModel(
    private val passwordLoginRepository: PasswordLoginRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _uiState = MutableStateFlow(PasswordLoginUiState())
    val uiState: StateFlow<PasswordLoginUiState> = _uiState.asStateFlow()

    fun onLoginChanged(value: String) {
        _uiState.update {
            it.copy(
                login = value,
                authState = if (it.authState is AuthFormState.Error) AuthFormState.Idle else it.authState,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                authState = if (it.authState is AuthFormState.Error) AuthFormState.Idle else it.authState,
            )
        }
    }

    fun onTermsChanged(accepted: Boolean) {
        _uiState.update {
            it.copy(
                termsAccepted = accepted,
                authState = if (it.authState is AuthFormState.Error) AuthFormState.Idle else it.authState,
            )
        }
    }

    fun submit() {
        val current = _uiState.value
        val login = current.login.trim()
        val password = current.password

        if (login.isEmpty()) {
            _uiState.update { it.copy(authState = AuthFormState.Error("Введите логин")) }
            return
        }
        if (password.isEmpty()) {
            _uiState.update { it.copy(authState = AuthFormState.Error("Введите пароль")) }
            return
        }
        if (!current.termsAccepted) {
            _uiState.update { it.copy(authState = AuthFormState.Error("Подтвердите согласие с условиями")) }
            return
        }

        coroutineScope.launch {
            _uiState.update { it.copy(authState = AuthFormState.Loading) }
            when (val result = passwordLoginRepository.login(login = login, password = password)) {
                PasswordLoginResult.Success -> {
                    _uiState.update { it.copy(authState = AuthFormState.Success(identifier = login)) }
                }
                is PasswordLoginResult.Error -> {
                    _uiState.update { it.copy(authState = AuthFormState.Error(result.message)) }
                }
            }
        }
    }
}
