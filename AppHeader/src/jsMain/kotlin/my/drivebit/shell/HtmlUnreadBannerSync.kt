package my.drivebit.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.document
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.UnreadMessagesViewModel
import org.koin.compose.koinInject

@Composable
fun HtmlUnreadBannerSync() {
    val storage: Storage = koinInject()
    val unreadMessagesViewModel: UnreadMessagesViewModel = koinInject()
    val hasUnread by unreadMessagesViewModel.hasUnread.collectAsState()

    LaunchedEffect(storage.isLogined(), hasUnread) {
        val banner = document.getElementById("drivebit-unread-banner") ?: return@LaunchedEffect
        if (storage.isLogined() && hasUnread) {
            banner.removeAttribute("hidden")
        } else {
            banner.setAttribute("hidden", "")
        }
    }
}
