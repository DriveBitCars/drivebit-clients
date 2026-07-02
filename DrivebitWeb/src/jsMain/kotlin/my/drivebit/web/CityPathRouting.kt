package my.drivebit.web

import my.drivebit.utils.cityNameToSlug

private const val ALL_FILTER_TITLE = "Все"

/**
 * Converts filter title into URL segment for city path.
 * "Все" is represented by city root path without a filter segment.
 */
private val CUSTOM_FILTER_PATH_SEGMENTS =
    mapOf(
        "Внедорожник" to "arenda-vnedorozhnika-bez-voditelya",
        "Минивэн" to "arenda-minivena-bez-voditelya",
    )

fun filterTitleToPathSegment(title: String): String? {
    val normalized = title.trim()
    if (normalized.isEmpty() || normalized == ALL_FILTER_TITLE) return null
    return CUSTOM_FILTER_PATH_SEGMENTS[normalized] ?: cityNameToSlug(normalized)
}

private val KNOWN_FILTER_TITLES =
    listOf(
        "Поблизости",
        "Путешествия",
        "За город",
        "К родным",
        "Командировки",
        "Каникулы",
        "Внедорожник",
        "Минивэн",
    )

fun filterTitleFromPathSegment(filterSlug: String?): String {
    if (filterSlug.isNullOrBlank()) return ALL_FILTER_TITLE
    val normalized = filterSlug.lowercase()
    CUSTOM_FILTER_PATH_SEGMENTS.entries
        .firstOrNull { it.value == normalized }
        ?.key
        ?.let { return it }
    return KNOWN_FILTER_TITLES.firstOrNull { filterTitleToPathSegment(it) == normalized }
        ?: ALL_FILTER_TITLE
}

/**
 * Builds path for city home + optional selected filter segment.
 */
fun cityPathWithFilter(
    citySlug: String,
    filterTitle: String,
): String {
    val filterSegment = filterTitleToPathSegment(filterTitle)
    return if (filterSegment == null) {
        "/$citySlug"
    } else {
        "/$citySlug/$filterSegment"
    }
}
