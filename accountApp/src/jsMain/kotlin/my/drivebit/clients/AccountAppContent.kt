package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.screens.BookingPaymentLinkPage
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePasswordPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.DocumentsPage
import my.drivebit.screens.DownloadBookingContractPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.LeaveReviewPage
import my.drivebit.screens.MyBookingsPage
import my.drivebit.screens.MyCitySelectionPage
import my.drivebit.screens.MyDealsPage
import my.drivebit.shared.storage.Storage
import my.drivebit.navigation.RedirectToLogin
import my.drivebit.web.homePathHref

@Composable
internal fun AccountAppContent(
    currentPath: String,
    storage: Storage,
) {
    when {
        currentPath.startsWith("/my-city-selection") -> {
            MyCitySelectionPage()
        }
        currentPath.startsWith("/my-bookings") -> {
            if (storage.isLogined()) {
                MyBookingsPage()
            } else {
                RedirectToHome(storage)
            }
        }
        currentPath.startsWith("/leave-review") -> {
            if (storage.isLogined()) {
                LeaveReviewPage()
            } else {
                RedirectToHome(storage)
            }
        }
        currentPath.startsWith("/my-deals") -> {
            if (storage.isLogined()) {
                MyDealsPage()
            } else {
                RedirectToHome(storage)
            }
        }
        currentPath.startsWith("/documents") -> {
            DocumentsPage()
        }
        currentPath.startsWith("/download-booking-contract") -> {
            DownloadBookingContractPage()
        }
        currentPath.startsWith("/payment") -> {
            BookingPaymentLinkPage()
        }
        currentPath.startsWith("/edit-name") -> {
            if (storage.isLogined()) {
                EditNamePage(currentPath)
            } else {
                RedirectToLogin()
            }
        }
        currentPath.startsWith("/change-email") -> {
            if (storage.isLogined()) {
                ChangeEmailPage()
            } else {
                RedirectToLogin()
            }
        }
        currentPath.startsWith("/change-phone") -> {
            if (storage.isLogined()) {
                ChangePhonePage()
            } else {
                RedirectToLogin()
            }
        }
        currentPath.startsWith("/change-password") -> {
            if (storage.isLogined()) {
                ChangePasswordPage()
            } else {
                RedirectToLogin()
            }
        }
    }
}

@Composable
private fun RedirectToHome(storage: Storage) {
    LaunchedEffect(storage) {
        window.location.href = homePathHref(storage)
    }
}
