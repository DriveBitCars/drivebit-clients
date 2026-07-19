package my.drivebit.utils

fun parseCitySlugFromSearchPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    val segments = trimmed.split('/').filter { it.isNotEmpty() }
    if (segments.size != 2) return null
    if (segments[1].lowercase() != "search") return null
    val citySlug = segments[0].lowercase()
    return citySlug.takeIf { it.isNotEmpty() }
}

fun buildCitySearchPath(
    citySlug: String,
    queryWithoutQuestionMark: String? = null,
): String {
    val base = "/${citySlug.trim().lowercase().trim('/')}/search"
    val query = queryWithoutQuestionMark?.trim()?.removePrefix("?")?.takeIf { it.isNotEmpty() }
    return if (query == null) base else "$base?$query"
}

fun isCitySearchPath(pathname: String): Boolean = parseCitySlugFromSearchPath(pathname) != null

/**
 * Bare `/search` → `/{city}/search` from MyCityRepository city name.
 * Missing/invalid city falls back to Moscow (`moskva`).
 */
fun resolveBareSearchRedirectPath(
    selectedCityName: String?,
    queryWithoutQuestionMark: String? = null,
): String {
    val slug =
        selectedCityName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::cityNameToSlug)
            ?.takeIf { it.isNotEmpty() && it != "city" }
            ?: "moskva"
    return buildCitySearchPath(slug, queryWithoutQuestionMark)
}
