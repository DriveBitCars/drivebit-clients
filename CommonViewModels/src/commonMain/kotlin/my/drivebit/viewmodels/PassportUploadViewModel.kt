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
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.CreateCarRepository

sealed interface PassportUploadState {
    data object Idle : PassportUploadState

    data object Uploading : PassportUploadState

    data object CreatingCar : PassportUploadState

    data class Error(
        val message: String,
    ) : PassportUploadState

    data class Success(
        val document: Document,
    ) : PassportUploadState

    data class CarCreated(
        val carId: String,
    ) : PassportUploadState
}

interface PassportUploadViewModel {
    val state: StateFlow<PassportUploadState>

    fun upload(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    )

    fun createCar()

    fun reset()
}

class PassportUploadViewModelImpl(
    private val documents: Documents,
    private val createCarRepository: CreateCarRepository,
    private val carDataRepository: CarDataRepository,
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

    override fun createCar() {
        viewModelScope.launch {
            val dailyRate = carDataRepository.getDailyRate() ?: return@launch

            _state.update { PassportUploadState.CreatingCar }

            createCarRepository
                .createCar(dailyRate)
                .onSuccess { response ->
                    _state.update { PassportUploadState.CarCreated(response.id) }
                }
                .onFailure { e ->
                    val message =
                        ErrorHandler.extractErrorMessage(
                            exception = e,
                            defaultNetworkError = "Ошибка сети",
                            defaultGenericError = "Не удалось создать автомобиль",
                        )
                    _state.update { PassportUploadState.Error(message) }
                }
        }
    }
}
