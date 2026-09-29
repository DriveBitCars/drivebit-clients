package my.drivebit.navigation

import kotlinx.browser.window
import my.drivebit.utils.stripWebBasePath
import my.drivebit.utils.withWebBasePath

class NavigationController(
    private val navigationState: NavigationState,
) {
    fun getCurrentPath(): String =
        stripWebBasePath(window.location.pathname, window.location.hostname)

    fun navigateTo(path: String) {
        val browserPath = withWebBasePath(path, window.location.hostname)
        if (shouldUseFullPageNavigation(path)) {
            window.location.href = fullPageNavigationHref(browserPath)
            return
        }
        window.history.pushState(null, "", browserPath)
        // Query lives in location.search; pathname-only avoids city routing misparsing "/search?..." as a slug.
        navigationState.updatePath(getCurrentPath())
    }

    fun replacePath(path: String) {
        val browserPath = withWebBasePath(path, window.location.hostname)
        if (shouldUseFullPageNavigation(path)) {
            window.location.replace(fullPageNavigationHref(browserPath))
            return
        }
        window.history.replaceState(null, "", browserPath)
        navigationState.updatePath(getCurrentPath())
    }

    fun goBack() {
        window.history.back()
    }

    fun goForward() {
        window.history.forward()
    }
}

private fun shouldUseFullPageNavigation(targetPath: String): Boolean =
    requiresFullPageNavigation(
        currentPathname = stripWebBasePath(window.location.pathname, window.location.hostname),
        targetPath = targetPath,
    )

private fun fullPageNavigationHref(targetPath: String): String {
    val hashStart = targetPath.indexOf('#')
    val queryStart = targetPath.indexOf('?')
    val pathOnly = pathWithoutQuery(targetPath)
    val search =
        when {
            queryStart >= 0 -> targetPath.substring(queryStart, if (hashStart >= 0) hashStart else targetPath.length)
            else -> ""
        }
    val hash = if (hashStart >= 0) targetPath.substring(hashStart) else ""
    return splitBundleHref(pathOnly, search, hash)
}
