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

sealed class OtpVerificationState {
    object Idle : OtpVerificationState()

    object Loading : OtpVerificationState()

    data class Success(
        val token: String?,
    ) : OtpVerificationState()

    data class Error(
        val message: String,
    ) : OtpVerificationState()
}

class OtpVerificationViewModel(
    private val auth: Auth,
    private val identifier: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val viewModelScope = coroutineScope

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _state = MutableStateFlow<OtpVerificationState>(OtpVerificationState.Idle)
    val state: StateFlow<OtpVerificationState> = _state.asStateFlow()

    fun updateCode(newCode: String) {
        val digitsOnly = newCode.filter { it.isDigit() }.take(6)
        _code.value = digitsOnly
        if (_state.value is OtpVerificationState.Error) {
            _state.update { OtpVerificationState.Idle }
        }
    }

    fun verifyOtp() {
        if (_code.value.length != 6) {
            _state.update { OtpVerificationState.Error("Введите 6-значный код") }
            return
        }

        viewModelScope.launch {
            _state.update { OtpVerificationState.Loading }
            try {
                val response = auth.verifyOtp(identifier, _code.value)
                _state.update { OtpVerificationState.Success(response.token) }
            } catch (e: Exception) {
                _state.update {
                    OtpVerificationState.Error(
                        e.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка",
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { OtpVerificationState.Idle }
    }
}
