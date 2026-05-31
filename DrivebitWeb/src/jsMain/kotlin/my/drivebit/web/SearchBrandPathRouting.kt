package my.drivebit.web

import my.drivebit.utils.cityNameToSlug

fun parseSearchBrandSlugFromPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (!trimmed.startsWith("search/")) return null
    val segment = trimmed.removePrefix("search/").substringBefore('/')
    if (segment.isEmpty()) return null
    return segment.lowercase()
}

fun searchPathForBrandName(brandName: String): String = "/search/${cityNameToSlug(brandName)}"

fun isSearchBrandPath(pathname: String): Boolean = parseSearchBrandSlugFromPath(pathname) != null
