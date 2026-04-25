package my.drivebit.navigation

import kotlinx.browser.window

class NavigationController(
    private val navigationState: NavigationState,
) {
    fun getCurrentPath(): String = window.location.pathname

    fun navigateTo(path: String) {
        window.history.pushState(null, "", path)
        // Query lives in location.search; pathname-only avoids city routing misparsing "/search?..." as a slug.
        navigationState.updatePath(window.location.pathname)
    }

    fun replacePath(path: String) {
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
