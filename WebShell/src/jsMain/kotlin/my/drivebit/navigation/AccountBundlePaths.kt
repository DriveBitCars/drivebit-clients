package my.drivebit.navigation

fun isAccountBundlePath(pathname: String): Boolean {
    val path = pathname.removeSuffix("/").ifEmpty { "/" }
    return path == "/my-city-selection" ||
        path.startsWith("/my-city-selection/") ||
        path == "/my-bookings" ||
        path.startsWith("/my-bookings/") ||
        path == "/leave-review" ||
        path.startsWith("/leave-review/") ||
        path == "/my-deals" ||
        path.startsWith("/my-deals/") ||
        path == "/documents" ||
        path.startsWith("/documents/") ||
        path == "/download-booking-contract" ||
        path.startsWith("/download-booking-contract/") ||
        path == "/payment" ||
        path.startsWith("/payment/") ||
        path == "/edit-name" ||
        path.startsWith("/edit-name/") ||
        path == "/change-email" ||
        path.startsWith("/change-email/") ||
        path == "/change-phone" ||
        path.startsWith("/change-phone/") ||
        path == "/change-password" ||
        path.startsWith("/change-password/") ||
        path == "/inspection-act" ||
        path.startsWith("/inspection-act/")
}

val accountBundleShellRoutes: List<String> =
    listOf(
        "my-city-selection",
        "my-bookings",
        "leave-review",
        "my-deals",
        "documents",
        "download-booking-contract",
        "payment",
        "edit-name",
        "change-email",
        "change-phone",
        "change-password",
        "inspection-act",
    )
