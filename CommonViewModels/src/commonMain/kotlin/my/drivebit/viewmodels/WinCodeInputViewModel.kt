package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult

class WinCodeInputViewModel(
    private val winCodeValidator: InputValidator,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _winCode = MutableStateFlow("")
    val winCode: StateFlow<String> = _winCode.asStateFlow()

    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Error("Введите WIN код"))
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    val isValid: Boolean
        get() = _validationState.value is ValidationState.Valid

    fun updateWinCode(newWinCode: String) {
        val uppercased = newWinCode.uppercase()
        _winCode.value = uppercased
        validateWinCode(uppercased)
    }

    private fun validateWinCode(winCode: String) {
        val validationResult = winCodeValidator.isValid(winCode)
        _validationState.update {
            when (validationResult) {
                is ValidationResult.Valid -> ValidationState.Valid
                is ValidationResult.Invalid -> ValidationState.Error(validationResult.errorMessage)
            }
        }
    }

    fun clearError() {
        if (_validationState.value is ValidationState.Error) {
            validateWinCode(_winCode.value)
        }
    }
}
