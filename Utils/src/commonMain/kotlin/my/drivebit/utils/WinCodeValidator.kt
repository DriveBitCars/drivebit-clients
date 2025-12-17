package my.drivebit.utils

class WinCodeValidator : InputValidator {
    override fun isValid(input: String): ValidationResult {
        val trimmed = input.trim().uppercase()

        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Введите WIN код")
        }

        if (trimmed.length != 17) {
            return ValidationResult.Invalid("WIN код должен содержать 17 символов")
        }

        val invalidChars = trimmed.filter { char ->
            !char.isLetterOrDigit() || char == 'I' || char == 'O' || char == 'Q'
        }

        if (invalidChars.isNotEmpty()) {
            return ValidationResult.Invalid("WIN код содержит недопустимые символы (I, O, Q не допускаются)")
        }

        return ValidationResult.Valid
    }
}

