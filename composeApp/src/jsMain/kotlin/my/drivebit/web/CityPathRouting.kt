package my.drivebit.web

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

/**
 * Returns slug for a city home URL `/slug` when [pathname] is exactly one non-reserved segment.
 */
fun parseCitySlugFromPath(pathname: String): String? {
    val trimmed = pathname.trim().removePrefix("/").removeSuffix("/")
    if (trimmed.isEmpty()) return null
    val segments = trimmed.split('/').filter { it.isNotEmpty() }
    if (segments.size != 1) return null
    val seg = segments[0].lowercase()
    if (seg in RESERVED_FIRST_SEGMENTS) return null
    return seg
}

fun isCityHomePath(pathname: String): Boolean {
    if (pathname.isEmpty() || pathname == "/") return true
    return parseCitySlugFromPath(pathname) != null
}
