package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import my.drivebit.web.login.loginWebModule
import my.drivebit.web.parseFilterSlugFromCityPath
import org.koin.compose.KoinApplication

@Composable
@Suppress("FunctionName")
fun NearbyApp() {
    KoinApplication(application = {
        modules(
            storageModule,
            repositoriesModule,
            webModule,
            loginWebModule,
            commonViewModelsModule,
        )
    }) {
        CookieConsentBanner()
        Navigation { currentPath ->
            if (parseFilterSlugFromCityPath(currentPath) == "poblizosti") {
                NearbyPage()
            } else {
                RedirectToMainApp(currentPath)
            }
        }
    }
}

@Composable
private fun RedirectToMainApp(currentPath: String) {
    LaunchedEffect(currentPath) {
        val search = window.location.search
        window.location.href = if (currentPath.isBlank() || currentPath == "/") "/" else currentPath + search
    }
}
