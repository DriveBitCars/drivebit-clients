package my.drivebit.navigation

import kotlinx.browser.window

class NavigationController(
    private val navigationState: NavigationState,
) {
    fun getCurrentPath(): String = window.location.pathname

    fun navigateTo(path: String) {
        if (shouldUseFullPageNavigation(path)) {
            window.location.href = fullPageNavigationHref(path)
            return
        }
        window.history.pushState(null, "", path)
        // Query lives in location.search; pathname-only avoids city routing misparsing "/search?..." as a slug.
        navigationState.updatePath(window.location.pathname)
    }

    fun replacePath(path: String) {
        if (shouldUseFullPageNavigation(path)) {
            window.location.replace(fullPageNavigationHref(path))
            return
        }
        window.history.replaceState(null, "", path)
        navigationState.updatePath(window.location.pathname)
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
        currentPathname = window.location.pathname,
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
