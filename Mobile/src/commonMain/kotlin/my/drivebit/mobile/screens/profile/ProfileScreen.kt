package my.drivebit.mobile.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.UserGetResponse
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.ui.components.Loader
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

class ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: ProfileViewModel = koinScreenModel()

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = "Мой профиль",
                    onBackClick = { navigator.pop() },
                )
            },
        ) { paddingValues ->
            ProfileScreenContent(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel,
            )
        }
    }
}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val currentState = state) {
            is ProfileState.Loading -> {
                Loader()
            }

            is ProfileState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Ошибка: ${currentState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { viewModel.loadProfile() }) {
                        Text("Повторить")
                    }
                }
            }

            is ProfileState.Success -> {
                val user = currentState.user

                if (user.firstName != null || user.lastName != null) {
                    Text(
                        text =
                            buildString {
                                user.firstName?.let { append(it) }
                                user.middleName?.let { append(" $it") }
                                user.lastName?.let { append(" $it") }
                            }.trim(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

                user.createdAt?.let { createdAt ->
                    Text(
                        text = "Присоединился: $createdAt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Button(onClick = { }) {
                    Text("Редактировать профиль")
                }

                Text(
                    text = "ПРОВЕРЕННАЯ ИНФОРМАЦИЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                user.phone?.let { phone ->
                    ProfileInfoRow(
                        label = "Номер телефона",
                        value = phone,
                        action = "Подтвердить номер телефона",
                    )
                }

                user.email?.let { email ->
                    ProfileInfoRow(
                        label = "Email адрес",
                        value = email,
                        action = "Подтверждено",
                    )
                }

                Text(
                    text = "ОТЗЫВЫ ОТ ХОСТОВ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "Пока нет отзывов",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = "Пока не получено отзывов на Drivebit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    action: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = action,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
@Preview
fun ProfileScreenPreview() {
    DrivebitTheme {
        ProfileScreenContent(
            viewModel =
                object : ProfileViewModel {
                    override val state: kotlinx.coroutines.flow.StateFlow<ProfileState> =
                        kotlinx.coroutines.flow
                            .MutableStateFlow(
                                ProfileState.Success(
                                    UserGetResponse(
                                        id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                        phone = "+7(912)742-88-27",
                                        firstName = "Anton",
                                        lastName = "B.",
                                        email = "user@example.com",
                                        createdAt = "2025-09-01T00:00:00Z",
                                        photos = emptyList(),
                                    ),
                                ),
                            ).asStateFlow()

                    override fun loadProfile() {}
                },
        )
    }
}
