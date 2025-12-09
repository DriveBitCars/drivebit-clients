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
import my.drivebit.network.services.Photo
import my.drivebit.repositories.AvatarRepository

sealed interface AvatarUploadState {
    data object Idle : AvatarUploadState

    data object Uploading : AvatarUploadState

    data class Error(
        val message: String,
    ) : AvatarUploadState

    data object Success : AvatarUploadState
}

expect class FileReader

interface AvatarUploadViewModel {
    val state: StateFlow<AvatarUploadState>

    fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    )

    fun uploadAvatarFile(file: FileReader)
}

class AvatarUploadViewModelImpl(
    private val photo: Photo,
    private val avatarRepository: AvatarRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : AvatarUploadViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<AvatarUploadState>(AvatarUploadState.Idle)

    override val state: StateFlow<AvatarUploadState>
        get() = _state.asStateFlow()

    override fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ) {
        viewModelScope.launch {
            _state.update { AvatarUploadState.Uploading }
            runCatching {
                photo.uploadAvatar(fileBytes, fileName, contentType)
            }.onSuccess {
                avatarRepository.refresh()
                _state.update { AvatarUploadState.Success }
            }.onFailure { e ->
                val errorMessage =
                    when (e) {
                        is NetworkException -> e.message
                        else -> e.message?.takeIf { it.isNotBlank() } ?: "Ошибка при загрузке файла"
                    }
                _state.update { AvatarUploadState.Error(errorMessage) }
            }
        }
    }

    override fun uploadAvatarFile(file: FileReader) {
        viewModelScope.launch {
            uploadAvatarFileImpl(file)
        }
    }
}

expect suspend fun AvatarUploadViewModelImpl.uploadAvatarFileImpl(file: FileReader)
