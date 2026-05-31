package my.drivebit.navigation

import kotlinx.browser.window
import my.drivebit.web.parseFilterSlugFromCityPath

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

private enum class AppIsland {
    Main,
    CarDetail,
    Nearby,
    Search,
}

private fun appIslandFor(path: String): AppIsland {
    val pathname = path.substringBefore('?').ifBlank { window.location.pathname }
    return when {
        pathname.startsWith("/car-detail") || pathname.startsWith("/car-photos-gallery") -> AppIsland.CarDetail
        parseFilterSlugFromCityPath(pathname) == "poblizosti" -> AppIsland.Nearby
        pathname.startsWith("/search") -> AppIsland.Search
        else -> AppIsland.Main
    }
}

private fun shouldUseFullPageNavigation(targetPath: String): Boolean {
    return appIslandFor(window.location.pathname) != appIslandFor(targetPath)
}
