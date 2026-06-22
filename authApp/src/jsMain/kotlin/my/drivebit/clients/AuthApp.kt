package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.RedirectToMainApp
import my.drivebit.navigation.RedirectToSplitBundle
import my.drivebit.navigation.isAnySplitBundlePath
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
            when {
                isAuthBundlePath(currentPath) -> {
                    AuthAppContent(currentPath = currentPath)
                }
                isAnySplitBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                else -> {
                    RedirectToMainApp()
                }
            }
        }
    }
}
