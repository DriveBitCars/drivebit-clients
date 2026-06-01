package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isOwnerCarBundlePath
import my.drivebit.shared.storage.Storage
import my.drivebit.web.koin.WebKoinHost
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
fun MyCarsApp() {
    WebKoinHost {
        CookieConsentBanner()
        Navigation { currentPath ->
            val storage: Storage = koinInject()
            if (isOwnerCarBundlePath(currentPath)) {
                OwnerCarAppContent(currentPath = currentPath, storage = storage)
            } else {
                RedirectToMainApp()
            }
        }
    }
}

@Composable
private fun RedirectToMainApp() {
    LaunchedEffect(Unit) {
        window.location.href = "/"
    }
}
