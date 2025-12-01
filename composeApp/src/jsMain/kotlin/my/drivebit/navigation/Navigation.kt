package my.drivebit.navigation

import kotlinx.browser.window

class NavigationController(
    private val navigationState: NavigationState,
) {
    fun getCurrentPath(): String = window.location.pathname

    fun navigateTo(path: String) {
        window.history.pushState(null, "", path)
        navigationState.updatePath(path)
    }

    fun replacePath(path: String) {
        window.history.replaceState(null, "", path)
        navigationState.updatePath(path)
    }

    fun goBack() {
        window.history.back()
    }

    fun goForward() {
        window.history.forward()
    }
}
