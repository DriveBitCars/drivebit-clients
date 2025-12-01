package my.drivebit.viewmodels

class EmailValidator : Validator {
    private val emailRegex =
        Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$",
            RegexOption.IGNORE_CASE,
        )

    override fun validate(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val normalized = trimmed.lowercase()
        return if (isValidEmail(normalized)) {
            normalized
        } else {
            trimmed.lowercase()
        }
    }

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email)
}
