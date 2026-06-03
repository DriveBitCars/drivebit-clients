package my.drivebit.clients

import androidx.compose.runtime.Composable
import my.drivebit.screens.LoginByPasswordPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.OtpVerificationPage
import org.koin.core.qualifier.named

@Composable
internal fun AuthAppContent(currentPath: String) {
    when {
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
    }
}
