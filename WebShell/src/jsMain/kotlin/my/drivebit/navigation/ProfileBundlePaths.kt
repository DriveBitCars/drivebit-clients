package my.drivebit.navigation

fun isProfileBundlePath(pathname: String): Boolean {
    val path = pathname.removeSuffix("/").ifEmpty { "/" }
    return path == "/profile" || path.startsWith("/profile/")
}

val profileBundleShellRoutes: List<String> =
    listOf(
        "profile",
    )
