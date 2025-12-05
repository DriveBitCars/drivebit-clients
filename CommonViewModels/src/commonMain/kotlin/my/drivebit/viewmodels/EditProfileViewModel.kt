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
    data object Loading : EditProfileState

    data class Error(
        val message: String,
    ) : EditProfileState

    data object Success : EditProfileState

    data class Initial(
        val firstName: String,
        val lastName: String,
        val middleName: String,
    ) : EditProfileState
}

interface EditProfileViewModel {
    val state: StateFlow<EditProfileState>
    val firstName: StateFlow<String>
    val lastName: StateFlow<String>
    val middleName: StateFlow<String>

    fun updateFirstName(value: String)

    fun updateLastName(value: String)

    fun updateMiddleName(value: String)

    fun save()
}

class EditProfileViewModelImpl(
    private val userService: User,
    initialFirstName: String = "",
    initialLastName: String = "",
    initialMiddleName: String = "",
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : EditProfileViewModel {
    private val viewModelScope = coroutineScope

    private val _firstName = MutableStateFlow(initialFirstName)
    override val firstName: StateFlow<String>
        get() = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow(initialLastName)
    override val lastName: StateFlow<String>
        get() = _lastName.asStateFlow()

    private val _middleName = MutableStateFlow(initialMiddleName)
    override val middleName: StateFlow<String>
        get() = _middleName.asStateFlow()

    private val _state =
        MutableStateFlow<EditProfileState>(
            EditProfileState.Initial(initialFirstName, initialLastName, initialMiddleName),
        )

    override val state: StateFlow<EditProfileState>
        get() = _state.asStateFlow()

    override fun updateFirstName(value: String) {
        _firstName.update { value }
    }

    override fun updateLastName(value: String) {
        _lastName.update { value }
    }

    override fun updateMiddleName(value: String) {
        _middleName.update { value }
    }

    override fun save() {
        viewModelScope.launch {
            _state.update { EditProfileState.Loading }
            runCatching {
                userService.updateUser(
                    firstName = _firstName.value.takeIf { it.isNotBlank() },
                    lastName = _lastName.value.takeIf { it.isNotBlank() },
                    middleName = _middleName.value.takeIf { it.isNotBlank() },
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
