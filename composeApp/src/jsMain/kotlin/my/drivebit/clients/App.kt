package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isChatBundlePath
import my.drivebit.navigation.isOwnerCarBundlePath
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePasswordPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.DocumentsPage
import my.drivebit.screens.DownloadBookingContractPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.LeaveReviewPage
import my.drivebit.screens.LoginByPasswordPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.MyBookingsPage
import my.drivebit.screens.MyCitySelectionPage
import my.drivebit.screens.MyDealsPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.BookingPaymentLinkPage
import my.drivebit.components.CookieConsentBanner
import my.drivebit.screens.ProfilePage
import my.drivebit.screens.SearchPage
import my.drivebit.shared.storage.Storage
import my.drivebit.shell.MountWebShell
import my.drivebit.shell.isStaticHtmlShellPath
import my.drivebit.web.homePathHref
import my.drivebit.web.isCityHomePath
import my.drivebit.web.koin.WebKoinHost
import my.drivebit.web.StaticFiltersShellSync
import my.drivebit.web.StaticHeroShellSync
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
    WebKoinHost {
        MountWebShell()
        CookieConsentBanner()
        Navigation { currentPath ->
            StaticHeroShellSync(currentPath)
            StaticFiltersShellSync(currentPath)
            val storage: Storage = koinInject()
            when {
                currentPath.startsWith("/list-your-car") -> {
                    RedirectToListYourCarHtml()
                }
                isOwnerCarBundlePath(currentPath) -> {
                    RedirectToOwnerCarBundle()
                }
                isChatBundlePath(currentPath) -> {
                    RedirectToChatBundle()
                }
                currentPath.startsWith("/my-city-selection") -> {
                    MyCitySelectionPage()
                }
                currentPath.startsWith("/verify-otp") -> {
                    OtpVerificationPage()
                }
                currentPath.startsWith("/login-by-phone") -> {
                    LoginPage(mviQualifier = named("phoneLoginMvi"))
                }
                currentPath.startsWith("/login-by-mail") -> {
                    LoginPage(mviQualifier = named("emailLoginMvi"))
                }
                currentPath.startsWith("/login-by-password") -> {
                    LoginByPasswordPage()
                }
                currentPath.startsWith("/profile") -> {
                    if (storage.isLogined()) {
                        ProfilePage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/my-bookings") -> {
                    if (storage.isLogined()) {
                        MyBookingsPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/leave-review") -> {
                    if (storage.isLogined()) {
                        LeaveReviewPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/my-deals") -> {
                    if (storage.isLogined()) {
                        MyDealsPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/documents") -> {
                    DocumentsPage()
                }
                currentPath.startsWith("/download-booking-contract") -> {
                    if (storage.isLogined()) {
                        DownloadBookingContractPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/payment") -> {
                    BookingPaymentLinkPage()
                }
                currentPath.startsWith("/edit-name") -> {
                    EditNamePage(currentPath)
                }
                currentPath.startsWith("/change-email") -> {
                    ChangeEmailPage()
                }
                currentPath.startsWith("/change-phone") -> {
                    ChangePhonePage()
                }
                currentPath.startsWith("/change-password") -> {
                    ChangePasswordPage()
                }
                currentPath.startsWith("/search") -> {
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

@Composable
private fun RedirectToOwnerCarBundle() {
    LaunchedEffect(Unit) {
        val path = window.location.pathname
        val normalizedPath = if (path.endsWith("/")) path else "$path/"
        window.location.replace(normalizedPath + window.location.search + window.location.hash)
    }
}

@Composable
private fun RedirectToChatBundle() {
    LaunchedEffect(Unit) {
        val path = window.location.pathname
        val normalizedPath = if (path.endsWith("/")) path else "$path/"
        window.location.replace(normalizedPath + window.location.search + window.location.hash)
    }
}
