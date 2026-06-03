package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isAuthBundlePath
import my.drivebit.shell.MountWebShell
import my.drivebit.web.koin.WebKoinHost

@Composable
@Suppress("FunctionName")
fun AuthApp() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            if (isAuthBundlePath(currentPath)) {
                AuthAppContent(currentPath = currentPath)
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
