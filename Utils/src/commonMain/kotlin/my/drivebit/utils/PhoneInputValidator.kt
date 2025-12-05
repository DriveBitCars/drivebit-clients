package my.drivebit.utils

class PhoneInputValidator : InputValidator {
    override fun isValid(input: String): ValidationResult {
        val hasNonDigits = input.any { !it.isDigit() && it != '+' && it != ' ' && it != '-' && it != '(' && it != ')' }
        if (hasNonDigits) {
            return ValidationResult.Invalid("Номер телефона должен содержать только цифры")
        }

        val digitsOnly = input.filter { it.isDigit() }

        if (digitsOnly.isEmpty()) {
            return ValidationResult.Invalid("Введите номер телефона")
        }

        val phoneDigits =
            when {
                digitsOnly.startsWith("7") -> digitsOnly.substring(1)
                digitsOnly.startsWith("8") -> digitsOnly.substring(1)
                else -> digitsOnly
            }

        if (phoneDigits.length != 10) {
            return ValidationResult.Invalid("Номер телефона должен содержать 10 цифр")
        }

        return ValidationResult.Valid
    }
}
