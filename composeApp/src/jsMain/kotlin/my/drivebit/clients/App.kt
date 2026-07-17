package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.components.CookieConsentBanner
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.RedirectToSplitBundle
import my.drivebit.navigation.isAccountBundlePath
import my.drivebit.navigation.isAuthBundlePath
import my.drivebit.navigation.isChatBundlePath
import my.drivebit.navigation.isOwnerCarBundlePath
import my.drivebit.navigation.isProfileBundlePath
import my.drivebit.screens.SearchPage
import my.drivebit.shell.MountWebShell
import my.drivebit.shell.isStaticHtmlShellPath
import my.drivebit.web.StaticFiltersShellSync
import my.drivebit.web.StaticHeroHeadlineSync
import my.drivebit.web.StaticHeroShellSync
import my.drivebit.web.StaticMainPromoShellSync
import my.drivebit.web.isCityHomePath
import my.drivebit.web.isSearchPath
import my.drivebit.web.koin.WebKoinHost

@Composable
@Suppress("FunctionName")
actual fun App() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            StaticHeroShellSync(currentPath)
            StaticHeroHeadlineSync(currentPath)
            StaticFiltersShellSync(currentPath)
            StaticMainPromoShellSync(currentPath)
            when {
                currentPath.startsWith("/list-your-car") -> {
                    RedirectToListYourCarHtml()
                }
                isOwnerCarBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                isChatBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                isAuthBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                isProfileBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                isAccountBundlePath(currentPath) -> {
                    RedirectToSplitBundle()
                }
                isSearchPath(currentPath) -> {
                    SearchPage()
                }
                isStaticHtmlShellPath(currentPath) -> Unit
                isCityHomePath(currentPath) -> HomePage()
                else -> HomePage()
            }
        }
    }
}

@Composable
private fun RedirectToListYourCarHtml() {
    LaunchedEffect(Unit) {
        window.location.replace("/list-your-car.html")
    }
}
