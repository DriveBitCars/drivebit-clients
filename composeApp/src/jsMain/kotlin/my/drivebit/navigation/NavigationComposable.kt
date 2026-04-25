package my.drivebit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import kotlinx.coroutines.flow.first
import my.drivebit.repositories.MyCityRepository
import my.drivebit.utils.cityNameToSlug
import my.drivebit.web.CitySlugResolver
import my.drivebit.web.parseCitySlugFromPath
import org.koin.compose.koinInject
import kotlin.js.asDynamic

@Composable
fun Navigation(content: @Composable (String) -> Unit) {
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()
    val myCityRepository: MyCityRepository = koinInject()
    val citySlugResolver: CitySlugResolver = koinInject()

    val navigationController =
        remember {
            NavigationController(navigationState)
        }

    LaunchedEffect(currentPath) {
        when {
            currentPath.isEmpty() || currentPath == "/" -> {
                val city = myCityRepository.getSelectedCity.first()
                val target = "/${cityNameToSlug(city.name)}"
                navigationController.replacePath(target)
            }
            else -> {
                val slug = parseCitySlugFromPath(currentPath) ?: return@LaunchedEffect
                val city =
                    citySlugResolver.resolve(slug) ?: run {
                        val fb = citySlugResolver.moscowCity()
                        navigationController.replacePath("/${cityNameToSlug(fb.name)}")
                        myCityRepository.selectCity(fb.id, fb.name)
                        return@LaunchedEffect
                    }
                val current = myCityRepository.getSelectedCity.first()
                if (current.id != city.id || current.name != city.name) {
                    myCityRepository.selectCity(city.id, city.name)
                }
            }
        }
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
