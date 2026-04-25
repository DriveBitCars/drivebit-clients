package my.drivebit.utils

/**
 * Stable URL slug from a Russian (or mixed) city name, e.g. "Москва" → "moskva".
 */
fun cityNameToSlug(name: String): String {
    val raw =
        buildString {
            val lower = name.trim().lowercase()
            for (ch in lower) {
                when {
                    ch.isWhitespace() || ch == '-' || ch == '_' || ch == '.' -> append(' ')
                    else -> append(transliterateChar(ch))
                }
            }
        }.trim()

    val slug =
        raw
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .joinToString("-") { segment ->
                segment.filter { it.isLetterOrDigit() }
            }.lowercase()
            .trim('-')

    return slug.ifEmpty { "city" }
}

@Suppress("CyclomaticComplexMethod")
private fun transliterateChar(ch: Char): String =
    when (ch) {
        in 'a'..'z',
        in '0'..'9',
        -> ch.toString()
        'а' -> "a"
        'б' -> "b"
        'в' -> "v"
        'г' -> "g"
        'д' -> "d"
        'е' -> "e"
        'ё' -> "yo"
        'ж' -> "zh"
        'з' -> "z"
        'и' -> "i"
        'й' -> "y"
        'к' -> "k"
        'л' -> "l"
        'м' -> "m"
        'н' -> "n"
        'о' -> "o"
        'п' -> "p"
        'р' -> "r"
        'с' -> "s"
        'т' -> "t"
        'у' -> "u"
        'ф' -> "f"
        'х' -> "h"
        'ц' -> "ts"
        'ч' -> "ch"
        'ш' -> "sh"
        'щ' -> "sch"
        'ъ',
        'ь',
        -> ""
        'ы' -> "y"
        'э' -> "e"
        'ю' -> "yu"
        'я' -> "ya"
        else -> ""
    }
