package my.drivebit.utils

/**
 * Brands that historically used bare `/{slug}` URLs.
 * Still accepted when parsing so old bookmarks keep working;
 * [pathForBrandSlug] / [canonicalizeBrandSearchPath] always emit `/search/{slug}`.
 */
val LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS: Set<String> = setOf("audi", "bmw")

@Deprecated("Use LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS", ReplaceWith("LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS"))
val SHORT_BRAND_SEARCH_PATH_SLUGS: Set<String> = LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS

fun pathForBrandSlug(slug: String): String {
    val normalized = slug.trim().lowercase()
    return "/search/$normalized"
}

fun parseBrandSlugFromPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("search/")) {
        val segment = trimmed.removePrefix("search/").substringBefore('/')
        return segment.takeIf { it.isNotEmpty() }?.lowercase()
    }
    val segment = trimmed.substringBefore('/').lowercase()
    return segment.takeIf { it in LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS }
}

fun canonicalizeBrandSearchPath(pathname: String): String? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    val segments = trimmed.split('/').filter { it.isNotEmpty() }.map { it.lowercase() }

    val brandSlug: String
    val modelSlug: String?
    when {
        segments.size >= 2 && segments[0] == "search" -> {
            brandSlug = segments[1]
            modelSlug = segments.getOrNull(2)?.takeIf { it.isNotEmpty() }
        }
        segments.isNotEmpty() && segments[0] in LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS -> {
            brandSlug = segments[0]
            modelSlug = segments.getOrNull(1)?.takeIf { it.isNotEmpty() }
        }
        else -> return null
    }
    if (brandSlug.isEmpty()) return null
    val base = pathForBrandSlug(brandSlug)
    return if (modelSlug != null) "$base/$modelSlug" else base
}

fun isShortBrandSearchPath(pathname: String): Boolean {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty() || '/' in trimmed) return false
    return trimmed.lowercase() in LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS
}
