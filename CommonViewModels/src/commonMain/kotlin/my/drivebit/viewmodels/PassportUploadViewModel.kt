package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents
import my.drivebit.repositories.CreateCarRepository

sealed interface PassportUploadState {
    data object Idle : PassportUploadState

    data object Uploading : PassportUploadState

    data class Error(
        val message: String,
    ) : PassportUploadState

    data class Success(
        val document: Document,
    ) : PassportUploadState
}

interface PassportUploadViewModel {
    val state: StateFlow<PassportUploadState>
    val createCarRepository: CreateCarRepository

    fun upload(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    )

    fun reset()
}

class PassportUploadViewModelImpl(
    private val documents: Documents,
    override val createCarRepository: CreateCarRepository,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : PassportUploadViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<PassportUploadState>(PassportUploadState.Idle)

    override val state: StateFlow<PassportUploadState>
        get() = _state.asStateFlow()

    override fun reset() {
        _state.update { PassportUploadState.Idle }
    }

    override fun upload(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ) {
        viewModelScope.launch {
            _state.update { PassportUploadState.Uploading }

            runCatching {
                documents.uploadDocument(
                    fileBytes = fileBytes,
                    fileName = fileName,
                    contentType = contentType,
                )
            }.onSuccess { doc ->
                _state.update { PassportUploadState.Success(doc) }
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить паспорт",
                    )
                _state.update { PassportUploadState.Error(message) }
            }
        }
    }
}
