package my.drivebit.navigation

import kotlinx.browser.window

class NavigationController(
    private val navigationState: NavigationState,
) {
    fun getCurrentPath(): String = window.location.pathname

    fun navigateTo(path: String) {
        if (shouldUseFullPageNavigation(path)) {
            window.location.href = path
            return
        }
        window.history.pushState(null, "", path)
        // Query lives in location.search; pathname-only avoids city routing misparsing "/search?..." as a slug.
        navigationState.updatePath(window.location.pathname)
    }

    fun replacePath(path: String) {
        if (shouldUseFullPageNavigation(path)) {
            window.location.replace(path)
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

private fun isSplitBundlePath(path: String): Boolean =
    path.startsWith("/car-detail") ||
        path.startsWith("/car-photos-gallery") ||
        path.startsWith("/my-cars")

private fun shouldUseFullPageNavigation(targetPath: String): Boolean {
    val currentPath = window.location.pathname
    val currentIsSplit = isSplitBundlePath(currentPath)
    val targetIsSplit = isSplitBundlePath(targetPath)
    return currentIsSplit != targetIsSplit
}
