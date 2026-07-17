package my.drivebit.web

import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.utils.buildCitySearchPath
import org.w3c.dom.url.URLSearchParams

fun formatHeroDateDisplay(isoDate: String): String {
    if (isoDate.isEmpty()) return ""
    val parts = isoDate.take(10).split("-")
    if (parts.size != 3) return isoDate
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}

fun buildHeroSearchUrl(
    startDate: String?,
    endDate: String?,
    citySlug: String = "moskva",
): String {
    val params = mutableListOf<String>()
    startDate?.takeIf { it.isNotEmpty() }?.let { params.add("startDate=${encodeURIComponent(it)}") }
    endDate?.takeIf { it.isNotEmpty() }?.let { params.add("endDate=${encodeURIComponent(it)}") }
    val query = params.joinToString("&").takeIf { it.isNotEmpty() }
    return buildCitySearchPath(citySlug, query)
}

private fun encodeURIComponent(value: String): String = js("encodeURIComponent")(value) as String

fun writeHeroDatesToQuery(
    startDate: String?,
    endDate: String?,
) {
    val params = URLSearchParams()
    startDate?.takeIf { it.isNotEmpty() }?.let { params.set("startDate", it) }
    endDate?.takeIf { it.isNotEmpty() }?.let { params.set("endDate", it) }
    val query = params.toString()
    val newUrl = window.location.pathname + if (query.isNotEmpty()) "?$query" else ""
    window.history.replaceState(null, "", newUrl)
    window.dispatchEvent(org.w3c.dom.CustomEvent("drivebit-hero-dates-changed"))
}

fun updateHeroDateDomDisplays(
    startDate: String?,
    endDate: String?,
) {
    updateHeroDateDisplayElement("drivebit-hero-start-display", startDate)
    updateHeroDateDisplayElement("drivebit-hero-end-display", endDate)
}

private fun updateHeroDateDisplayElement(
    elementId: String,
    isoDate: String?,
) {
    val element = document.getElementById(elementId) ?: return
    if (isoDate.isNullOrEmpty()) {
        element.textContent = "выберите даты"
        element.classList.add("is-empty")
    } else {
        element.textContent = formatHeroDateDisplay(isoDate)
        element.classList.remove("is-empty")
    }
}

fun readHeroDatesFromQuery(): Pair<String?, String?> = parseHeroDatesFromQuery(window.location.search)

fun applyHeroDatesFromQueryToDom() {
    val (start, end) = readHeroDatesFromQuery()
    updateHeroDateDomDisplays(start, end)
}
