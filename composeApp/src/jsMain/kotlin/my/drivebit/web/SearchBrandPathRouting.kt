package my.drivebit.web

import my.drivebit.utils.cityNameToSlug

private val RESERVED_SEARCH_SEGMENTS =
    setOf(
        "ai",
    )

fun parseSearchBrandSlugFromPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (!trimmed.startsWith("search/")) return null
    val segment = trimmed.removePrefix("search/").substringBefore('/')
    if (segment.isEmpty() || segment.lowercase() in RESERVED_SEARCH_SEGMENTS) return null
    return segment.lowercase()
}

fun searchPathForBrandName(brandName: String): String = "/search/${cityNameToSlug(brandName)}"

fun isSearchBrandPath(pathname: String): Boolean = parseSearchBrandSlugFromPath(pathname) != null
