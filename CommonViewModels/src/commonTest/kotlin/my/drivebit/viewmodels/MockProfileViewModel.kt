package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.UserGetResponse

class MockProfileViewModel(
    private val initialState: ProfileState =
        ProfileState.Success(
            UserGetResponse(
                id = "mock-id-123",
                phone = "+7(912)742-88-27",
                firstName = "Иван",
                middleName = "Петрович",
                lastName = "Иванов",
                email = "ivan.ivanov@example.com",
                createdAt = "2024-01-15T10:30:00Z",
                photos = emptyList(),
            ),
        ),
) : ProfileViewModel {
    private val _state = MutableStateFlow<ProfileState>(initialState)
    override val state: StateFlow<ProfileState> = _state.asStateFlow()

    override fun loadProfile() {
        _state.value = ProfileState.Loading
        _state.value = initialState
    }

    fun setState(newState: ProfileState) {
        _state.value = newState
    }
}
