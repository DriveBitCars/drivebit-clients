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

sealed interface EditProfileState {
    data class Error(
        val message: String,
    ) : EditProfileState

    data object Success : EditProfileState

    data class Initial(
        val firstName: String,
        val lastName: String,
        val middleName: String,
        val isLoading: Boolean,
    ) : EditProfileState {
        val isButtonEnabled: Boolean
            get() = firstName.isNotBlank()
    }
}

interface EditProfileViewModel {
    val state: StateFlow<EditProfileState>

    fun updateFirstName(value: String)

    fun updateLastName(value: String)

    fun updateMiddleName(value: String)

    fun save()
}

class EditProfileViewModelImpl(
    private val userService: User,
    initialFirstName: String,
    initialLastName: String,
    initialMiddleName: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : EditProfileViewModel {
    private val viewModelScope = coroutineScope

    private val _state =
        MutableStateFlow<EditProfileState>(
            EditProfileState.Initial(initialFirstName, initialLastName, initialMiddleName, isLoading = false),
        )

    override val state: StateFlow<EditProfileState>
        get() = _state.asStateFlow()

    override fun updateFirstName(value: String) {
        _state.update { currentState ->
            when (currentState) {
                is EditProfileState.Initial -> {
                    currentState.copy(firstName = value)
                }
                else -> currentState
            }
        }
    }

    override fun updateLastName(value: String) {
        _state.update { currentState ->
            when (currentState) {
                is EditProfileState.Initial -> {
                    currentState.copy(lastName = value)
                }
                else -> currentState
            }
        }
    }

    override fun updateMiddleName(value: String) {
        _state.update { currentState ->
            when (currentState) {
                is EditProfileState.Initial -> {
                    currentState.copy(middleName = value)
                }
                else -> currentState
            }
        }
    }

    override fun save() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is EditProfileState.Initial) {
                _state.update { currentState.copy(isLoading = true) }
                runCatching {
                    userService.updateUser(
                        firstName = currentState.firstName.ifBlank { null },
                        lastName = currentState.lastName.ifBlank { null },
                        middleName = currentState.middleName.ifBlank { null },
                    )
                }.onSuccess {
                    _state.update { EditProfileState.Success }
                }.onFailure { e ->
                    val errorMessage =
                        when (e) {
                            is NetworkException -> e.message
                            else -> e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
                        }
                    _state.update { EditProfileState.Error(errorMessage) }
                }
            }
        }
    }
}
