package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.RedirectToMainApp
import my.drivebit.navigation.RedirectToSplitBundle
import my.drivebit.navigation.isAnySplitBundlePath
import my.drivebit.screens.CarDetailPage
import my.drivebit.screens.CarPhotosGalleryPage
import my.drivebit.shell.MountWebShell
import my.drivebit.web.koin.WebKoinHost

@Composable
@Suppress("FunctionName")
fun CarDetailApp() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            when {
                currentPath.startsWith("/car-photos-gallery") -> {
                    CarPhotosGalleryPage()
                }
                currentPath.startsWith("/car-detail") -> {
                    CarDetailPage()
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
