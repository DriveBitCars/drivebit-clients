package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarPhotoResponse
import my.drivebit.network.services.Photo
import my.drivebit.network.services.sortedResponsesBySortOrder

enum class PhotoMoveDirection {
    Up,
    Down,
}

sealed interface CarPhotosState {
    data object Loading : CarPhotosState

    data class Error(
        val message: String,
    ) : CarPhotosState

    data class Success(
        val photos: List<CarPhotoResponse>,
        val isUploading: Boolean = false,
        val uploadError: String? = null,
        val isReordering: Boolean = false,
    ) : CarPhotosState
}

interface CarPhotosViewModel {
    val state: StateFlow<CarPhotosState>

    fun loadPhotos(carId: String)

    fun uploadPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    )

    fun deletePhoto(
        carId: String,
        photoId: Int,
    )

    fun movePhoto(
        carId: String,
        photoId: Int,
        direction: PhotoMoveDirection,
    )
}

class CarPhotosViewModelImpl(
    private val photoService: Photo,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarPhotosViewModel {
    private val _state = MutableStateFlow<CarPhotosState>(CarPhotosState.Loading)
    private var isLoadingInProgress = false

    override val state: StateFlow<CarPhotosState>
        get() = _state.asStateFlow()

    override fun loadPhotos(carId: String) {
        if (isLoadingInProgress) return

        coroutineScope.launch {
            isLoadingInProgress = true
            _state.value = CarPhotosState.Loading
            runCatching {
                val photos = photoService.getCarPhotos(carId)
                _state.value = CarPhotosState.Success(photos)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить фотографии",
                    )
                _state.value = CarPhotosState.Error(errorMessage)
            }.also {
                isLoadingInProgress = false
            }
        }
    }

    override fun uploadPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ) {
        val currentState = _state.value
        if (currentState !is CarPhotosState.Success) return

        coroutineScope.launch {
            _state.update {
                when (it) {
                    is CarPhotosState.Success -> it.copy(isUploading = true, uploadError = null)
                    else -> it
                }
            }

            runCatching {
                photoService.uploadCarPhotos(
                    carId = carId,
                    fileBytesList = fileBytesList,
                    fileNames = fileNames,
                    contentTypes = contentTypes,
                )
                loadPhotos(carId)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить фотографии",
                    )
                _state.update {
                    when (it) {
                        is CarPhotosState.Success -> it.copy(isUploading = false, uploadError = errorMessage)
                        else -> it
                    }
                }
            }
        }
    }

    override fun deletePhoto(
        carId: String,
        photoId: Int,
    ) {
        coroutineScope.launch {
            runCatching {
                photoService.deleteCarPhoto(photoId)
                loadPhotos(carId)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось удалить фотографию",
                    )
                _state.update {
                    when (it) {
                        is CarPhotosState.Success -> it.copy(uploadError = errorMessage)
                        else -> it
                    }
                }
            }
        }
    }

    override fun movePhoto(
        carId: String,
        photoId: Int,
        direction: PhotoMoveDirection,
    ) {
        val current = _state.value
        if (current !is CarPhotosState.Success || current.isReordering) return
        val index = current.photos.indexOfFirst { it.id == photoId }
        if (index < 0) return
        val swapWith =
            when (direction) {
                PhotoMoveDirection.Up -> index - 1
                PhotoMoveDirection.Down -> index + 1
            }
        if (swapWith !in current.photos.indices) return

        val previous = current.photos
        val reordered =
            previous
                .toMutableList()
                .apply {
                    val tmp = this[index]
                    this[index] = this[swapWith]
                    this[swapWith] = tmp
                }.mapIndexed { i, photo -> photo.copy(sortOrder = i + 1) }

        coroutineScope.launch {
            _state.value =
                current.copy(
                    photos = reordered,
                    isReordering = true,
                    uploadError = null,
                )
            runCatching {
                photoService.reorderCarPhotos(carId, reordered.map { it.id })
            }.onSuccess { serverPhotos ->
                _state.update {
                    when (it) {
                        is CarPhotosState.Success ->
                            it.copy(
                                photos = serverPhotos.sortedResponsesBySortOrder(),
                                isReordering = false,
                                uploadError = null,
                            )
                        else -> it
                    }
                }
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось изменить порядок фотографий",
                    )
                _state.update {
                    when (it) {
                        is CarPhotosState.Success ->
                            it.copy(
                                photos = previous,
                                isReordering = false,
                                uploadError = message,
                            )
                        else -> it
                    }
                }
            }
        }
    }
}
