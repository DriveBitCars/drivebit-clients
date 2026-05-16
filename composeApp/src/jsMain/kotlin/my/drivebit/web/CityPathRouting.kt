package my.drivebit.web

import my.drivebit.utils.cityNameToSlug

/**
 * First path segments handled by [my.drivebit.clients.App] — not city slugs.
 */
val RESERVED_FIRST_SEGMENTS: Set<String> =
    setOf(
        "city-selection",
        "my-city-selection",
        "list-your-car",
        "verify-otp",
        "login-by-phone",
        "login-by-mail",
        "login-by-password",
        "signup",
        "profile",
        "my-cars",
        "my-bookings",
        "leave-review",
        "my-deals",
        "chats",
        "chat",
        "documents",
        "offer",
        "contacts",
        "privacy",
        "cookies",
        "payment",
        "payment-success",
        "payment-failure",
        "car-edit",
        "car-photos-gallery",
        "car-photos-upload",
        "car-photos",
        "edit-name",
        "change-email",
        "change-phone",
        "change-password",
        "address-input",
        "license-plate-input",
        "car-brand-selection",
        "car-model-selection",
        "body-type-selection",
        "drive-type-selection",
        "engine-type-selection",
        "engine-volume-input",
        "production-year-input",
        "seats-count-input",
        "trunk-size-selection",
        "daily-rate-input",
        "description-input",
        "passport-upload",
        "search",
        "car-detail",
    )

private const val ALL_FILTER_TITLE = "Все"

data class CityPathParts(
    val citySlug: String,
    val filterSlug: String? = null,
)

/**
 * Parses city URL path:
 * - `/city-slug`
 * - `/city-slug/filter-slug`
 */
fun parseCityPath(pathname: String): CityPathParts? {
    val pathOnly = pathname.substringBefore('?').substringBefore('#')
    val trimmed = pathOnly.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    val segments = trimmed.split('/').filter { it.isNotEmpty() }
    if (segments.size != 1 && segments.size != 2) return null
    val citySlug = segments[0].lowercase()
    if (citySlug in RESERVED_FIRST_SEGMENTS) return null
    val filterSlug = segments.getOrNull(1)?.lowercase()
    return CityPathParts(citySlug = citySlug, filterSlug = filterSlug)
}

/**
 * Returns slug for a city home URL when first segment is a non-reserved city slug.
 */
fun parseCitySlugFromPath(pathname: String): String? = parseCityPath(pathname)?.citySlug

/**
 * Returns optional filter slug from city path `/city-slug/filter-slug`.
 */
fun parseFilterSlugFromCityPath(pathname: String): String? = parseCityPath(pathname)?.filterSlug

/**
 * Converts filter title into URL segment for city path.
 * "Все" is represented by city root path without a filter segment.
 */
fun filterTitleToPathSegment(title: String): String? {
    val normalized = title.trim()
    if (normalized.isEmpty() || normalized == ALL_FILTER_TITLE) return null
    return cityNameToSlug(normalized)
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

fun isCityHomePath(pathname: String): Boolean {
    if (pathname.isEmpty() || pathname == "/") return true
    return parseCitySlugFromPath(pathname) != null
}
