package my.drivebit.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.document
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.UnreadMessagesViewModel
import org.koin.compose.koinInject

private const val UNREAD_BANNER_VISIBLE_CLASS = "drivebit-unread-banner--visible"
private const val HEADER_UNREAD_VISIBLE_CLASS = "drivebit-app-header--unread-visible"

@Composable
fun HtmlUnreadBannerSync() {
    val storage: Storage = koinInject()
    val unreadMessagesViewModel: UnreadMessagesViewModel = koinInject()
    val hasUnread by unreadMessagesViewModel.hasUnread.collectAsState()

    LaunchedEffect(storage.isLogined(), hasUnread) {
        val banner = document.getElementById("drivebit-unread-banner") ?: return@LaunchedEffect
        val header = document.getElementById("drivebit-app-header")
        val showBanner = storage.isLogined() && hasUnread
        if (showBanner) {
            banner.classList.add(UNREAD_BANNER_VISIBLE_CLASS)
            header?.classList?.add(HEADER_UNREAD_VISIBLE_CLASS)
        } else {
            banner.classList.remove(UNREAD_BANNER_VISIBLE_CLASS)
            header?.classList?.remove(HEADER_UNREAD_VISIBLE_CLASS)
        }
    }
}
