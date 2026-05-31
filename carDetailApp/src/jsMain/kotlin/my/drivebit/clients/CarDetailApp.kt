package my.drivebit.clients

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.screens.CarDetailPage
import my.drivebit.screens.CarPhotosGalleryPage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import my.drivebit.web.login.loginWebModule
import org.koin.compose.KoinApplication

@Composable
@Suppress("FunctionName")
fun CarDetailApp() {
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
            when {
                currentPath.startsWith("/car-photos-gallery") -> {
                    CarPhotosGalleryPage()
                }
                currentPath.startsWith("/car-detail") -> {
                    CarDetailPage()
                }
                else -> {
                    window.location.href = "/"
                }
            }
        }
    }
}
