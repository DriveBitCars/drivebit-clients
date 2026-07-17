package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import my.drivebit.utils.isShortBrandSearchPath
import org.w3c.dom.HTMLElement

fun isSearchPath(pathname: String): Boolean {
    val normalized = pathname.removeSuffix("/").ifEmpty { "/" }
    return normalized == "/search" ||
        normalized.startsWith("/search/") ||
        isShortBrandSearchPath(normalized)
}

fun isMyCitySelectionPath(pathname: String): Boolean {
    val normalized = pathname.removeSuffix("/").ifEmpty { "/" }
    return normalized == "/my-city-selection"
}

fun shouldShowStaticHeroShell(pathname: String): Boolean = isCityHomePath(pathname)

@Composable
fun StaticHeroShellSync(currentPath: String) {
    LaunchedEffect(currentPath) {
        val wrap = document.querySelector(".drivebit-hero-wrap") as? HTMLElement ?: return@LaunchedEffect
        wrap.style.display =
            if (shouldShowStaticHeroShell(currentPath)) {
                ""
            } else {
                "none"
            }
    }
}
