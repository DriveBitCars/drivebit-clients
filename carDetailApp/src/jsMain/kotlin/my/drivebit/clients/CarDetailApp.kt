package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.screens.CarDetailPage
import my.drivebit.screens.CarPhotosGalleryPage
import my.drivebit.web.koin.WebKoinHost

@Composable
@Suppress("FunctionName")
fun CarDetailApp() {
    WebKoinHost {
        CookieConsentBanner()
        Navigation { currentPath ->
            when {
                currentPath.startsWith("/car-photos-gallery") -> {
                    CarPhotosGalleryPage()
                }
                currentPath.startsWith("/car-detail") -> {
                    CarDetailPage()
                }
                else -> {
                    RedirectToMainApp(currentPath)
                }
            }
        }
    }
}

@Composable
private fun RedirectToMainApp(currentPath: String) {
    LaunchedEffect(currentPath) {
        window.location.href = "/"
    }
}
