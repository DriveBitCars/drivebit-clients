package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isProfileBundlePath
import my.drivebit.shell.MountWebShell
import my.drivebit.web.koin.WebKoinHost

@Composable
@Suppress("FunctionName")
fun ProfileApp() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            if (isProfileBundlePath(currentPath)) {
                ProfileAppContent(currentPath = currentPath)
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
