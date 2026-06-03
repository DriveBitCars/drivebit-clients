package my.drivebit.navigation

fun isAuthBundlePath(pathname: String): Boolean {
    val path = pathname.removeSuffix("/").ifEmpty { "/" }
    return path == "/verify-otp" ||
        path.startsWith("/verify-otp/") ||
        path == "/login-by-phone" ||
        path.startsWith("/login-by-phone/") ||
        path == "/login-by-mail" ||
        path.startsWith("/login-by-mail/") ||
        path == "/login-by-password" ||
        path.startsWith("/login-by-password/")
}

val authBundleShellRoutes: List<String> =
    listOf(
        "verify-otp",
        "login-by-phone",
        "login-by-mail",
        "login-by-password",
    )
