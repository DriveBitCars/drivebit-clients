package my.drivebit.utils

const val HOME_DEFAULT_NEARBY_RADIUS_KM = 10

data class HomeUrlParts(
    val citySlug: String,
    val filterSlug: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val page: Int = 1,
    val lat: Double? = null,
    val lon: Double? = null,
    val radiusKm: Int? = null,
)

fun parseHomeUrl(url: String): HomeUrlParts {
    val pathOnly = url.substringBefore('?').substringBefore('#')
    val query = url.substringAfter('?', missingDelimiterValue = "").substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    val segments = trimmed.split('/').filter { it.isNotEmpty() }.map { it.lowercase() }

    val citySlug = segments.firstOrNull().orEmpty()
    val second = segments.getOrNull(1)
    val filterSlug =
        second
            ?.takeIf { it.isNotEmpty() && it != "search" }

    val params = parseHomeQueryParams(query)
    return HomeUrlParts(
        citySlug = citySlug,
        filterSlug = filterSlug,
        startDate = params["startDate"]?.takeIf { it.isNotEmpty() },
        endDate = params["endDate"]?.takeIf { it.isNotEmpty() },
        page = params["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        lat = params["lat"]?.toDoubleOrNull(),
        lon = params["lon"]?.toDoubleOrNull(),
        radiusKm = params["radiusKm"]?.toIntOrNull(),
    )
}

fun buildHomeUrl(parts: HomeUrlParts): String {
    val city =
        parts.citySlug
            .trim()
            .lowercase()
            .trim('/')
    val filter =
        parts.filterSlug
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() && it != "search" }
    val path =
        if (filter != null) {
            "/$city/$filter"
        } else {
            "/$city"
        }

    val queryPairs = mutableListOf<Pair<String, String>>()

    fun add(
        key: String,
        value: String?,
    ) {
        value?.takeIf { it.isNotEmpty() }?.let { queryPairs.add(key to it) }
    }

    add("startDate", parts.startDate)
    add("endDate", parts.endDate)
    if (parts.page > 1) {
        queryPairs.add("page" to parts.page.toString())
    }
    parts.lat?.let { queryPairs.add("lat" to formatHomeDouble(it)) }
    parts.lon?.let { queryPairs.add("lon" to formatHomeDouble(it)) }
    parts.radiusKm
        ?.takeIf { it != HOME_DEFAULT_NEARBY_RADIUS_KM }
        ?.let { queryPairs.add("radiusKm" to it.toString()) }

    if (queryPairs.isEmpty()) return path
    val query =
        queryPairs.joinToString("&") { (k, v) ->
            "$k=${encodeQueryComponent(v)}"
        }
    return "$path?$query"
}

private fun formatHomeDouble(value: Double): String {
    val asLong = value.toLong()
    return if (value == asLong.toDouble()) {
        asLong.toString()
    } else {
        value.toString()
    }
}

private fun parseHomeQueryParams(query: String): Map<String, String> {
    if (query.isEmpty()) return emptyMap()
    return query
        .split('&')
        .mapNotNull { pair ->
            if (pair.isEmpty()) return@mapNotNull null
            val key = pair.substringBefore('=')
            val value = pair.substringAfter('=', missingDelimiterValue = "")
            if (key.isEmpty()) null else key to decodeQueryComponent(value)
        }.toMap()
}
