package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.utils.stripWebBasePath
import my.drivebit.utils.withWebBasePath

fun isAnySplitBundlePath(pathname: String): Boolean {
    val path = pathWithoutQuery(pathname)
    return isProfileBundlePath(path) ||
        isAccountBundlePath(path) ||
        isAuthBundlePath(path) ||
        isChatBundlePath(path) ||
        isOwnerCarBundlePath(path) ||
        path.startsWith("/car-detail") ||
        path.startsWith("/car-photos-gallery") ||
        my.drivebit.web.isSearchPath(path)
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
): String {
    val host = window.location.hostname
    val appPath = stripWebBasePath(pathname, host)
    return withWebBasePath(splitBundleHref(appPath, search, hash), host)
}

@Composable
fun RedirectToSplitBundle() {
    LaunchedEffect(Unit) {
        window.location.replace(splitBundleHrefFromLocation())
    }
}

@Composable
fun RedirectToMainApp() {
    LaunchedEffect(Unit) {
        window.location.href = withWebBasePath("/", window.location.hostname)
    }
}

@Composable
fun RedirectToLogin() {
    LaunchedEffect(Unit) {
        window.location.href = withWebBasePath("/login-by-phone", window.location.hostname)
    }
}
