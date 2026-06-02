package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.screens.ChatDetailPage
import my.drivebit.screens.ChatListPage
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref

@Composable
internal fun ChatsAppContent(
    currentPath: String,
    storage: Storage,
) {
    if (!storage.isLogined()) {
        LaunchedEffect(Unit) {
            window.location.href = homePathHref(storage)
        }
        return
    }

    when {
        currentPath.startsWith("/chats") -> {
            ChatListPage()
        }
        currentPath.startsWith("/chat") -> {
            ChatDetailPage()
        }
    }
}
