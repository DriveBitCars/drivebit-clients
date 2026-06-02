package my.drivebit.web

import org.w3c.dom.url.URLSearchParams

fun parseHeroDatesFromQuery(search: String): Pair<String?, String?> {
    if (search.isEmpty()) return null to null
    val params = URLSearchParams(search)
    val start = params.get("startDate")?.takeIf { it.isNotEmpty() }
    val end = params.get("endDate")?.takeIf { it.isNotEmpty() }
    return start to end
}
