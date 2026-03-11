package my.drivebit.mobile.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import my.drivebit.network.services.MessageDto
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.ui.components.Loader
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.ChatDetailViewModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

data class ChatScreen(
    val chatId: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koinScope = currentKoinScope()
        val viewModel: ChatDetailViewModel =
            remember(chatId) {
                koinScope.get<ChatDetailViewModel>(parameters = { parametersOf(chatId) })
            }
        val chatDetail by viewModel.chatDetail.collectAsState()
        val messages by viewModel.messages.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        var messageText by remember { mutableStateOf("") }

        LaunchedEffect(chatId) {
            viewModel.loadChat()
            viewModel.loadMessages()
        }

        LaunchedEffect(chatId) {
            while (true) {
                delay(10_000)
                viewModel.loadMessages()
            }
        }

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = chatDetail?.participant?.name?.takeIf { it.isNotBlank() } ?: "Чат",
                    onBackClick = { navigator.pop() },
                )
            },
        ) { innerPadding ->
            when {
                isLoading && messages.isEmpty() -> Loader()
                error != null -> Text("Ошибка: $error", modifier = Modifier.padding(innerPadding))
                else ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding =
                                androidx.compose.foundation.layout
                                    .PaddingValues(16.dp),
                        ) {
                            items(messages.reversed()) { message ->
                                MessageBubble(
                                    message = message,
                                    participantId = chatDetail?.participant?.id,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val focusManager = LocalFocusManager.current
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text("Введите сообщение...") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions =
                                    KeyboardActions(
                                        onSend = {
                                            if (messageText.isNotBlank()) {
                                                viewModel.sendMessage(messageText.trim())
                                                messageText = ""
                                                focusManager.clearFocus()
                                            }
                                        },
                                    ),
                            )
                            androidx.compose.material3.Button(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        viewModel.sendMessage(messageText.trim())
                                        messageText = ""
                                    }
                                },
                            ) {
                                Text("Отправить")
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageDto,
    participantId: String? = null,
) {
    val senderId = message.sender?.id
    val isOwnMessage = participantId != null && senderId != null && senderId != participantId
    val displayName =
        when {
            message.isSystemMessage -> null
            isOwnMessage -> "Вы"
            else -> message.sender?.name?.takeIf { it.isNotBlank() }
        }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (!message.isSystemMessage) {
            displayName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = message.text ?: "",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = runCatching { mapIso8601ToTimeString(message.createdAt) }.getOrElse { "" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = message.text ?: "Системное сообщение",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
