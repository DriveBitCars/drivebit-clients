package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.navigation.Navigation
import my.drivebit.navigation.isOwnerCarBundlePath
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePasswordPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.ChatDetailPage
import my.drivebit.screens.ChatListPage
import my.drivebit.screens.ContactsPage
import my.drivebit.screens.CookiesPage
import my.drivebit.screens.DocumentsPage
import my.drivebit.screens.DownloadBookingContractPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.LeaveReviewPage
import my.drivebit.screens.LoginByPasswordPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.MyBookingsPage
import my.drivebit.screens.MyCitySelectionPage
import my.drivebit.screens.MyDealsPage
import my.drivebit.screens.OfferPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.BookingPaymentLinkPage
import my.drivebit.screens.PaymentFailurePage
import my.drivebit.screens.PaymentSuccessPage
import my.drivebit.screens.PrivacyPage
import my.drivebit.components.CookieConsentBanner
import my.drivebit.screens.ProfilePage
import my.drivebit.screens.SearchPage
import my.drivebit.shared.storage.Storage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import my.drivebit.web.homePathHref
import my.drivebit.web.login.loginWebModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
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
            val storage: Storage = koinInject()
            when {
                currentPath.startsWith("/list-your-car") -> {
                    RedirectToListYourCarHtml()
                }
                isOwnerCarBundlePath(currentPath) -> {
                    RedirectToOwnerCarBundle()
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
                currentPath.startsWith("/chats") -> {
                    if (storage.isLogined()) {
                        ChatListPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/chat") -> {
                    if (storage.isLogined()) {
                        ChatDetailPage()
                    } else {
                        window.location.href = homePathHref(storage)
                    }
                }
                currentPath.startsWith("/documents") -> {
                    DocumentsPage()
                }
                currentPath.startsWith("/offer") -> {
                    OfferPage()
                }
                currentPath.startsWith("/contacts") -> {
                    ContactsPage()
                }
                currentPath.startsWith("/privacy") -> {
                    PrivacyPage()
                }
                currentPath.startsWith("/cookies") -> {
                    CookiesPage()
                }
                currentPath.startsWith("/payment-success") -> {
                    PaymentSuccessPage()
                }
                currentPath.startsWith("/payment-failure") -> {
                    PaymentFailurePage()
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
                else -> {
                    HomePage()
                }
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
