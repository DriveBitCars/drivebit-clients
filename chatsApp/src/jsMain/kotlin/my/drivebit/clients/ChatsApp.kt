package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isChatBundlePath
import my.drivebit.shared.storage.Storage
import my.drivebit.shell.MountWebShell
import my.drivebit.web.koin.WebKoinHost
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
fun ChatsApp() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            val storage: Storage = koinInject()
            if (isChatBundlePath(currentPath)) {
                ChatsAppContent(currentPath = currentPath, storage = storage)
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
