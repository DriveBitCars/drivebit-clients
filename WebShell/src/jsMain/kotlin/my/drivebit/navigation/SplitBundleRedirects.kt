package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window

fun isAnySplitBundlePath(pathname: String): Boolean {
    val path = pathWithoutQuery(pathname)
    return isProfileBundlePath(path) ||
        isAccountBundlePath(path) ||
        isAuthBundlePath(path) ||
        isChatBundlePath(path) ||
        isOwnerCarBundlePath(path) ||
        path.startsWith("/car-detail") ||
        path.startsWith("/car-photos-gallery")
}

fun splitBundleHref(
    path: String,
    search: String = "",
    hash: String = "",
): String {
    val pathOnly = pathWithoutQuery(path)
    val normalizedPath = if (pathOnly.endsWith("/")) pathOnly else "$pathOnly/"
    return normalizedPath + search + hash
}

fun splitBundleHrefFromLocation(
    pathname: String = window.location.pathname,
    search: String = window.location.search,
    hash: String = window.location.hash,
): String = splitBundleHref(pathname, search, hash)

@Composable
fun RedirectToSplitBundle() {
    LaunchedEffect(Unit) {
        window.location.replace(splitBundleHrefFromLocation())
    }
}

@Composable
fun RedirectToMainApp() {
    LaunchedEffect(Unit) {
        window.location.href = "/"
    }
}

@Composable
fun RedirectToLogin() {
    LaunchedEffect(Unit) {
        window.location.href = "/login-by-phone"
    }
}
