package my.drivebit.viewmodels

object LicensePlateValidator {
    const val MAX_LENGTH = 9

    private val ALLOWED_LETTERS =
        setOf(
            'А',
            'В',
            'Е',
            'К',
            'М',
            'Н',
            'О',
            'Р',
            'С',
            'Т',
            'У',
            'Х',
        )

    private val ALLOWED_CHARS = ALLOWED_LETTERS + ('0'..'9').toSet()

    private val PLATE_REGEX =
        Regex(
            "^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
            RegexOption.IGNORE_CASE,
        )

    const val ERROR_MESSAGE =
        "Некорректный формат госномера. Формат: X123XX12 или X123XX123 (X — буквы А,В,Е,К,М,Н,О,Р,С,Т,У,Х)"

    fun isValid(plate: String): Boolean = PLATE_REGEX.matches(plate.trim())

    fun filterInput(input: String): String =
        input
            .uppercase()
            .filter { it in ALLOWED_CHARS }
            .take(MAX_LENGTH)
}
