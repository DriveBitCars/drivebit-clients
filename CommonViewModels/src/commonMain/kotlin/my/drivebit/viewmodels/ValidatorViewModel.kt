package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult
import my.drivebit.utils.Validator

class ValidatorViewModel(
    private val validator: Validator,
    private val inputValidator: InputValidator,
    initialErrorMessage: String = "",
) {
    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Error(initialErrorMessage))
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    val isValid: Boolean
        get() = _validationState.value is ValidationState.Valid

    fun formatInput(input: String): String = validator.validate(input)

    fun validateInput(input: String) {
        val validationResult = inputValidator.isValid(input)
        _validationState.update {
            when (validationResult) {
                is ValidationResult.Valid -> ValidationState.Valid
                is ValidationResult.Invalid -> ValidationState.Error(validationResult.errorMessage)
            }
        }
    }

    fun setError(message: String) {
        _validationState.update { ValidationState.Error(message) }
    }

    fun clearError() {
        _validationState.update { ValidationState.Valid }
    }
}
