package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.RedirectToMainApp
import my.drivebit.navigation.RedirectToSplitBundle
import my.drivebit.navigation.isAnySplitBundlePath
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
            when {
                isChatBundlePath(currentPath) -> {
                    ChatsAppContent(currentPath = currentPath, storage = storage)
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
