package my.drivebit.utils

class PhoneValidator : Validator {
    override fun validate(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return "+7"
        val phoneDigits =
            when {
                digitsOnly.startsWith("7") -> digitsOnly.substring(1).take(10)
                digitsOnly.startsWith("8") -> digitsOnly.substring(1).take(10)
                else -> digitsOnly.take(10)
            }
        return "+7$phoneDigits"
    }
}
