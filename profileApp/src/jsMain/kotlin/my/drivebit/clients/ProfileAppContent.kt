package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.screens.ProfilePage
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref
import org.koin.compose.koinInject

@Composable
internal fun ProfileAppContent(
    currentPath: String,
) {
    val storage: Storage = koinInject()
    when {
        currentPath.startsWith("/profile") -> {
            if (storage.isLogined()) {
                ProfilePage()
            } else {
                RedirectToHome(storage)
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
