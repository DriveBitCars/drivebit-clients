package my.drivebit.viewmodels

sealed class ValidationResult {
    object Valid : ValidationResult()

    data class Invalid(
        val errorMessage: String,
    ) : ValidationResult()
}

interface InputValidator {
    fun isValid(input: String): ValidationResult
}
