package my.drivebit.utils

val SHORT_BRAND_SEARCH_PATH_SLUGS: Set<String> = setOf("audi", "bmw")

fun pathForBrandSlug(slug: String): String {
    val normalized = slug.trim().lowercase()
    return if (normalized in SHORT_BRAND_SEARCH_PATH_SLUGS) {
        "/$normalized"
    } else {
        "/search/$normalized"
    }
}

fun parseBrandSlugFromPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("search/")) {
        val segment = trimmed.removePrefix("search/").substringBefore('/')
        return segment.takeIf { it.isNotEmpty() }?.lowercase()
    }
    if ('/' in trimmed) return null
    val segment = trimmed.lowercase()
    return segment.takeIf { it in SHORT_BRAND_SEARCH_PATH_SLUGS }
}

fun canonicalizeBrandSearchPath(pathname: String): String? {
    val slug = parseBrandSlugFromPath(pathname) ?: return null
    return pathForBrandSlug(slug)
}

fun isShortBrandSearchPath(pathname: String): Boolean {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty() || '/' in trimmed) return false
    return trimmed.lowercase() in SHORT_BRAND_SEARCH_PATH_SLUGS
}
