package my.drivebit.clients.demo

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.mobile.screens.profile.ProfileScreenContent
import my.drivebit.network.services.UserGetResponse
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.ui.theme.DrivebitTheme
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel

class MockProfileViewModelForTest : ProfileViewModel {
    private val _state =
        MutableStateFlow<ProfileState>(
            ProfileState.Success(
                UserGetResponse(
                    id = "test-id-123",
                    phone = "+7(495) 877-50-51",
                    firstName = "Иван",
                    middleName = "Петрович",
                    lastName = "Иванов",
                    email = "ivan.ivanov@example.com",
                    createdAt = "2024-01-15T10:30:00Z",
                    photos = emptyList(),
                ),
            ),
        )
    override val state: StateFlow<ProfileState> = _state.asStateFlow()

    override fun loadProfile() {
        _state.value = ProfileState.Loading
        _state.value =
            ProfileState.Success(
                UserGetResponse(
                    id = "test-id-123",
                    phone = "+7(495) 877-50-51",
                    firstName = "Иван",
                    middleName = "Петрович",
                    lastName = "Иванов",
                    email = "ivan.ivanov@example.com",
                    createdAt = "2024-01-15T10:30:00Z",
                    photos = emptyList(),
                ),
            )
    }

    override fun refresh() {}
}

class ProfileScreenTest : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = MockProfileViewModelForTest()

        DrivebitTheme {
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
}
