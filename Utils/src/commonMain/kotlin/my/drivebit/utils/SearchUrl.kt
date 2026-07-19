package my.drivebit.utils

data class SearchUrlParts(
    val citySlug: String? = null,
    val brandSlug: String? = null,
    val modelSlug: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val dailyRateMin: Int? = null,
    val dailyRateMax: Int? = null,
    val driveType: String? = null,
    val driveTypeLabel: String? = null,
    val bodyType: String? = null,
    val bodyTypeLabel: String? = null,
    val seatsMin: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val mileageMin: Int? = null,
    val page: Int = 1,
)

fun parseSearchUrl(url: String): SearchUrlParts {
    val pathOnly = url.substringBefore('?').substringBefore('#')
    val query = url.substringAfter('?', missingDelimiterValue = "").substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    val segments = trimmed.split('/').filter { it.isNotEmpty() }.map { it.lowercase() }

    var citySlug: String? = null
    var brandSlug: String? = null
    var modelSlug: String? = null

    when {
        segments.size == 2 && segments[1] == "search" -> {
            citySlug = segments[0]
        }
        segments.size >= 2 && segments[0] == "search" -> {
            brandSlug = segments[1].takeIf { it.isNotEmpty() }
            modelSlug = segments.getOrNull(2)?.takeIf { it.isNotEmpty() }
        }
        segments.size >= 1 && segments[0] in LEGACY_SHORT_BRAND_SEARCH_PATH_SLUGS -> {
            brandSlug = segments[0]
            modelSlug = segments.getOrNull(1)?.takeIf { it.isNotEmpty() }
        }
        segments.size == 1 && segments[0] == "search" -> {
            // bare /search
        }
    }

    val params = parseQueryParams(query)
    return SearchUrlParts(
        citySlug = citySlug,
        brandSlug = brandSlug,
        modelSlug = modelSlug,
        startDate = params["startDate"],
        endDate = params["endDate"],
        dailyRateMin = params["dailyRateMin"]?.toIntOrNull(),
        dailyRateMax = params["dailyRateMax"]?.toIntOrNull(),
        driveType = params["driveType"],
        driveTypeLabel = params["driveTypeLabel"],
        bodyType = params["bodyType"],
        bodyTypeLabel = params["bodyTypeLabel"],
        seatsMin = params["seatsMin"]?.toIntOrNull(),
        yearMin = params["yearMin"]?.toIntOrNull(),
        yearMax = params["yearMax"]?.toIntOrNull(),
        mileageMin = params["mileageMin"]?.toIntOrNull(),
        page = params["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1,
    )
}

fun buildSearchUrl(parts: SearchUrlParts): String {
    val path =
        when {
            parts.brandSlug != null -> {
                val brandPath = pathForBrandSlug(parts.brandSlug)
                val model =
                    parts.modelSlug
                        ?.trim()
                        ?.lowercase()
                        ?.takeIf { it.isNotEmpty() }
                if (model != null) "$brandPath/$model" else brandPath
            }
            parts.citySlug != null -> {
                "/${parts.citySlug.trim().lowercase().trim('/')}/search"
            }
            else -> "/search"
        }

    val queryPairs = mutableListOf<Pair<String, String>>()

    fun add(
        key: String,
        value: String?,
    ) {
        value?.takeIf { it.isNotEmpty() }?.let { queryPairs.add(key to it) }
    }

    fun addInt(
        key: String,
        value: Int?,
    ) {
        value?.let { queryPairs.add(key to it.toString()) }
    }
    add("startDate", parts.startDate)
    add("endDate", parts.endDate)
    addInt("dailyRateMin", parts.dailyRateMin)
    addInt("dailyRateMax", parts.dailyRateMax)
    add("driveType", parts.driveType)
    add("driveTypeLabel", parts.driveTypeLabel)
    add("bodyType", parts.bodyType)
    add("bodyTypeLabel", parts.bodyTypeLabel)
    addInt("seatsMin", parts.seatsMin)
    addInt("yearMin", parts.yearMin)
    addInt("yearMax", parts.yearMax)
    addInt("mileageMin", parts.mileageMin)
    if (parts.page > 1) {
        queryPairs.add("page" to parts.page.toString())
    }

    if (queryPairs.isEmpty()) return path
    val query =
        queryPairs.joinToString("&") { (k, v) ->
            "$k=${encodeQueryComponent(v)}"
        }
    return "$path?$query"
}

private fun parseQueryParams(query: String): Map<String, String> {
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

internal fun encodeQueryComponent(value: String): String =
    buildString(value.length) {
        for (ch in value) {
            when (ch) {
                ' ' -> append("%20")
                '!' -> append("%21")
                '"' -> append("%22")
                '#' -> append("%23")
                '$' -> append("%24")
                '%' -> append("%25")
                '&' -> append("%26")
                '\'' -> append("%27")
                '(' -> append("%28")
                ')' -> append("%29")
                '*' -> append("%2A")
                '+' -> append("%2B")
                ',' -> append("%2C")
                '/' -> append("%2F")
                ':' -> append("%3A")
                ';' -> append("%3B")
                '=' -> append("%3D")
                '?' -> append("%3F")
                '@' -> append("%40")
                '[' -> append("%5B")
                ']' -> append("%5D")
                else -> {
                    if (ch.code < 0x80 && (ch.isLetterOrDigit() || ch == '-' || ch == '_' || ch == '.' || ch == '~')) {
                        append(ch)
                    } else {
                        val bytes = ch.toString().encodeToByteArray()
                        for (b in bytes) {
                            append('%')
                            append(((b.toInt() shr 4) and 0xF).toString(16).uppercase())
                            append((b.toInt() and 0xF).toString(16).uppercase())
                        }
                    }
                }
            }
        }
    }

internal fun decodeQueryComponent(value: String): String {
    val out = StringBuilder(value.length)
    var i = 0
    while (i < value.length) {
        val c = value[i]
        when {
            c == '+' -> {
                out.append(' ')
                i++
            }
            c == '%' && i + 2 < value.length -> {
                val hex = value.substring(i + 1, i + 3)
                val parsed = hex.toIntOrNull(16)
                if (parsed != null) {
                    val bytes = mutableListOf(parsed.toByte())
                    i += 3
                    while (i + 2 < value.length && value[i] == '%') {
                        val nextHex = value.substring(i + 1, i + 3)
                        val next = nextHex.toIntOrNull(16) ?: break
                        bytes.add(next.toByte())
                        i += 3
                    }
                    out.append(bytes.toByteArray().decodeToString())
                } else {
                    out.append(c)
                    i++
                }
            }
            else -> {
                out.append(c)
                i++
            }
        }
    }
    return out.toString()
}
