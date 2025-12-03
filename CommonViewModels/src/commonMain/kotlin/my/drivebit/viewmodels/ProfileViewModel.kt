package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.NetworkException
import my.drivebit.network.services.User
import my.drivebit.network.services.UserGetResponse

sealed interface ProfileState {
    data object Loading : ProfileState

    data class Error(
        val message: String,
    ) : ProfileState

    data class Success(
        val user: UserGetResponse,
    ) : ProfileState
}

interface ProfileViewModel {
    val state: StateFlow<ProfileState>

    fun loadProfile()
}

class ProfileViewModelImpl(
    private val userService: User,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ProfileViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)

    override val state: StateFlow<ProfileState>
        get() = _state.asStateFlow()

    init {
        loadProfile()
    }

    override fun loadProfile() {
        viewModelScope.launch {
            _state.update { ProfileState.Loading }
            runCatching {
                userService.userGet()
            }.onSuccess { userData ->
                _state.update { ProfileState.Success(userData) }
            }.onFailure { e ->
                val errorMessage =
                    when (e) {
                        is NetworkException -> e.message
                        else -> e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
                    }
                _state.update { ProfileState.Error(errorMessage) }
            }
        }
    }
}
