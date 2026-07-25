package my.drivebit.utils

data class BodyTypePathInfo(
    val slug: String,
    val apiName: String,
    val label: String,
)

val BODY_TYPE_SEARCH_PATH_ENTRIES: List<BodyTypePathInfo> =
    listOf(
        BodyTypePathInfo(slug = "sedan", apiName = "Sedan", label = "Седан"),
        BodyTypePathInfo(slug = "hatchback", apiName = "Hatchback", label = "Хэтчбек"),
        BodyTypePathInfo(slug = "crossover", apiName = "Crossover", label = "Кроссовер"),
        BodyTypePathInfo(slug = "suv", apiName = "SUV", label = "Внедорожник"),
        BodyTypePathInfo(slug = "minivan", apiName = "Minivan", label = "Минивэн"),
    )

private val bySlug: Map<String, BodyTypePathInfo> =
    BODY_TYPE_SEARCH_PATH_ENTRIES.associateBy { it.slug }

private val byApiNameLower: Map<String, BodyTypePathInfo> =
    BODY_TYPE_SEARCH_PATH_ENTRIES.associateBy { it.apiName.lowercase() }

fun bodyTypePathInfoBySlug(slug: String): BodyTypePathInfo? {
    val normalized = slug.trim().lowercase()
    if (normalized.isEmpty()) return null
    return bySlug[normalized]
}

fun bodyTypePathInfoByApiName(apiName: String): BodyTypePathInfo? {
    val normalized = apiName.trim().lowercase()
    if (normalized.isEmpty()) return null
    return byApiNameLower[normalized]
}

fun pathForBodyTypeApiName(apiName: String): String? {
    val info = bodyTypePathInfoByApiName(apiName) ?: return null
    return "/search/${info.slug}"
}

fun isBodyTypeSearchSlug(slug: String): Boolean = bodyTypePathInfoBySlug(slug) != null
