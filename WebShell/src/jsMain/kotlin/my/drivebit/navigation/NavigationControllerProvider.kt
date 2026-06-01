package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import kotlin.js.asDynamic
import org.koin.compose.koinInject

@Composable
fun NavigationControllerProvider(content: @Composable (String) -> Unit) {
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()

    val navigationController =
        remember {
            NavigationController(navigationState)
        }

    DisposableEffect(Unit) {
        val handler: (org.w3c.dom.events.Event) -> Unit = {
            navigationState.updatePath(window.location.pathname)
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

    CompositionLocalProvider(LocalNavigationController provides navigationController) {
        content(currentPath)
    }
}
