package my.drivebit.mobile.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.datetime.Instant
import my.drivebit.network.services.ChatListDto
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.ui.components.Loader
import my.drivebit.utils.formatRelativeTime
import my.drivebit.viewmodels.ChatListViewModel
import org.koin.compose.koinInject

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: ChatListViewModel = koinInject()
        val chats by viewModel.chats.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadChats()
        }

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = "Входящие",
                    onBackClick = { navigator.pop() },
                )
            },
        ) { innerPadding ->
            when {
                isLoading && chats.isEmpty() -> Loader()
                error != null -> Text("Ошибка: $error")
                chats.isEmpty() ->
                    Text(
                        "У вас пока нет сообщений",
                        modifier = Modifier.padding(innerPadding).padding(16.dp),
                    )
                else ->
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(16.dp),
                    ) {
                        items(chats) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { navigator.push(ChatScreen(chatId = chat.id)) },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatListDto,
    onClick: () -> Unit,
) {
    val participantName = chat.participant.name?.takeIf { it.isNotBlank() } ?: "Собеседник"
    val lastMessageText = chat.lastMessage?.text?.takeIf { it.isNotBlank() } ?: "Нет сообщений"
    val relativeTime =
        runCatching {
            chat.lastMessage?.createdAt?.let { formatRelativeTime(Instant.parse(it)) } ?: ""
        }.getOrElse { "" }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(
                        text = participantName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = participantName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = lastMessageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (chat.unreadCount > 0) {
                    Text(
                        text = "${chat.unreadCount} новых",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
