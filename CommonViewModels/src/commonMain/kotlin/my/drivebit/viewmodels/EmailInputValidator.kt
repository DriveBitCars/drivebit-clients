package my.drivebit.viewmodels

class EmailInputValidator : InputValidator {
    private val emailRegex =
        Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
            RegexOption.IGNORE_CASE,
        )

    override fun isValid(input: String): ValidationResult {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Введите email")
        }

        val normalized = trimmed.lowercase()

        if (!emailRegex.matches(normalized)) {
            return ValidationResult.Invalid("Введите корректный email адрес")
        }

        return ValidationResult.Valid
    }
}
