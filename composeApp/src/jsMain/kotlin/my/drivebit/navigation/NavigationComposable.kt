package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.koin.compose.koinInject
import kotlin.js.asDynamic

@Composable
fun Navigation(content: @Composable (String) -> Unit) {
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()

    val navigationController =
        remember {
            NavigationController(navigationState)
        }

    DisposableEffect(Unit) {
        MetaTags.updateForPath(currentPath)

        val handler: (org.w3c.dom.events.Event) -> Unit = {
            val newPath = window.location.pathname
            navigationState.updatePath(newPath)
            MetaTags.updateForPath(newPath)
        }

        val pageShowHandler: (org.w3c.dom.events.Event) -> Unit = { ev ->
            val persisted = ev.asDynamic().persisted as? Boolean
            if (persisted == true) {
                navigationState.notifyWindowShowRestoreFromCache()
            }
        }

        window.addEventListener("popstate", handler)
        window.addEventListener("pageshow", pageShowHandler)

        onDispose {
            window.removeEventListener("popstate", handler)
            window.removeEventListener("pageshow", pageShowHandler)
        }
    }

    DisposableEffect(currentPath) {
        MetaTags.updateForPath(currentPath)
        onDispose { }
    }

    CompositionLocalProvider(LocalNavigationController provides navigationController) {
        content(currentPath)
    }
}
