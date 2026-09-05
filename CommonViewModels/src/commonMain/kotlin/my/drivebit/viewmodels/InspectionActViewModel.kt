package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingInspectionActDownloadDto
import my.drivebit.network.services.BookingInspectionActDto
import my.drivebit.network.services.BookingInspectionActPhotoDto
import my.drivebit.network.services.InspectionAct
import my.drivebit.network.services.InspectionActType
import my.drivebit.network.services.InspectionActViewerRole
import my.drivebit.network.services.InspectionPhotoKind
import my.drivebit.network.services.UpdateInspectionCommentRequest
import my.drivebit.network.services.UpdateInspectionMetricsRequest
import my.drivebit.network.services.User
import my.drivebit.network.services.canCurrentUserEditComment
import my.drivebit.network.services.canCurrentUserEditMetrics
import my.drivebit.network.services.canCurrentUserUploadPhotos
import my.drivebit.network.services.commentInputFor
import my.drivebit.network.services.resolveInspectionActViewerRole

data class InspectionActPhotoFile(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String,
)

sealed interface InspectionActUiState {
    data object Idle : InspectionActUiState

    data object Loading : InspectionActUiState

    data class Ready(
        val act: BookingInspectionActDto,
        val viewerRole: InspectionActViewerRole?,
        val ownerId: String,
        val renterId: String,
        val ownerName: String,
        val renterName: String,
        val fuelInput: String,
        val mileageInput: String,
        val commentInput: String,
    ) : InspectionActUiState

    data class Error(
        val message: String,
        val previousAct: BookingInspectionActDto? = null,
        val viewerRole: InspectionActViewerRole? = null,
        val ownerId: String = "",
        val renterId: String = "",
        val ownerName: String = "",
        val renterName: String = "",
    ) : InspectionActUiState
}

enum class InspectionActAction {
    OpenOrCreate,
    UploadPhoto,
    DeletePhoto,
    Sign,
    DownloadPdf,
}

sealed interface InspectionActEffect {
    data class OpenPdf(
        val download: BookingInspectionActDownloadDto,
    ) : InspectionActEffect
}

interface InspectionActViewModel {
    val state: StateFlow<InspectionActUiState>
    val error: StateFlow<String?>
    val photoUploadStatus: StateFlow<String?>
    val actionsInProgress: StateFlow<Set<InspectionActAction>>
    val effects: SharedFlow<InspectionActEffect>

    fun load()

    fun setFuelInput(value: String)

    fun setMileageInput(value: String)

    fun setCommentInput(value: String)

    fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    )

    fun uploadPhotos(
        files: List<InspectionActPhotoFile>,
        kind: InspectionPhotoKind,
    )

    fun deletePhoto(photoId: String)

    fun sign()

    fun downloadPdf()
}

class InspectionActViewModelImpl(
    private val inspectionAct: InspectionAct,
    private val booking: Booking,
    private val user: User,
    private val bookingId: String,
    private val type: InspectionActType,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : InspectionActViewModel {
    private val _state = MutableStateFlow<InspectionActUiState>(InspectionActUiState.Idle)
    override val state: StateFlow<InspectionActUiState> = _state.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _photoUploadStatus = MutableStateFlow<String?>(null)
    override val photoUploadStatus: StateFlow<String?> = _photoUploadStatus.asStateFlow()

    private val _actionsInProgress = MutableStateFlow<Set<InspectionActAction>>(emptySet())
    override val actionsInProgress: StateFlow<Set<InspectionActAction>> = _actionsInProgress.asStateFlow()

    private val _effects = MutableSharedFlow<InspectionActEffect>(extraBufferCapacity = 1)
    override val effects: SharedFlow<InspectionActEffect> = _effects.asSharedFlow()

    private var lastAct: BookingInspectionActDto? = null
    private var viewerRole: InspectionActViewerRole? = null
    private var ownerId: String = ""
    private var renterId: String = ""
    private var ownerName: String = ""
    private var renterName: String = ""

    override fun load() {
        if (!begin(InspectionActAction.OpenOrCreate)) return
        if (bookingId.isBlank()) {
            finish(InspectionActAction.OpenOrCreate)
            showError("Не указан номер бронирования", lastAct)
            return
        }
        _state.value = InspectionActUiState.Loading
        _error.value = null
        coroutineScope.launch {
            try {
                val currentUser = user.userGet()
                val bookingDto = booking.getById(bookingId)
                viewerRole =
                    resolveInspectionActViewerRole(
                        currentUserId = currentUser.id,
                        ownerId = bookingDto.ownerId,
                        renterId = bookingDto.renterId,
                    )
                ownerId = bookingDto.ownerId
                renterId = bookingDto.renterId
                ownerName = bookingDto.ownerName?.takeIf { it.isNotBlank() }.orEmpty()
                renterName = bookingDto.renterName?.takeIf { it.isNotBlank() }.orEmpty()
                applyAct(inspectionAct.openOrCreate(bookingId, type))
            } catch (exception: Throwable) {
                showError(errorMessage(exception, "Не удалось загрузить акт"), lastAct)
            } finally {
                finish(InspectionActAction.OpenOrCreate)
            }
        }
    }

    override fun setFuelInput(value: String) {
        updateReady { it.copy(fuelInput = value) }
    }

    override fun setMileageInput(value: String) {
        updateReady { it.copy(mileageInput = value) }
    }

    override fun setCommentInput(value: String) {
        updateReady { it.copy(commentInput = value) }
    }

    override fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    ) {
        uploadPhotos(
            files = listOf(InspectionActPhotoFile(bytes, fileName, contentType)),
            kind = kind,
        )
    }

    override fun uploadPhotos(
        files: List<InspectionActPhotoFile>,
        kind: InspectionPhotoKind,
    ) {
        if (files.isEmpty()) return
        val ready = state.value as? InspectionActUiState.Ready ?: return
        if (!ready.act.canCurrentUserUploadPhotos(ready.viewerRole ?: return)) return
        if (!begin(InspectionActAction.UploadPhoto)) return
        _photoUploadStatus.value = "Загрузка 0 из ${files.size}…"
        coroutineScope.launch {
            try {
                val uploaded = mutableListOf<BookingInspectionActPhotoDto>()
                for ((index, file) in files.withIndex()) {
                    _photoUploadStatus.value = "Загрузка ${index + 1} из ${files.size}…"
                    try {
                        uploaded +=
                            inspectionAct.uploadPhoto(
                                bookingId = bookingId,
                                type = type,
                                fileBytes = file.bytes,
                                fileName = file.fileName,
                                contentType = file.contentType,
                                kind = kind,
                            )
                    } catch (_: Throwable) {
                        // continue remaining files; status reflects partial success below
                    }
                }
                val current = lastAct
                if (current != null && uploaded.isNotEmpty()) {
                    applyAct(current.copy(photos = current.photos.orEmpty() + uploaded))
                }
                _photoUploadStatus.value =
                    when {
                        uploaded.size == files.size -> "Загружено фото: ${uploaded.size}"
                        uploaded.isEmpty() -> "Не удалось загрузить фото"
                        else -> "Загружено ${uploaded.size} из ${files.size}"
                    }
                if (uploaded.isEmpty()) {
                    showError("Не удалось загрузить фото", lastAct)
                } else {
                    _error.value = null
                }
            } finally {
                finish(InspectionActAction.UploadPhoto)
            }
        }
    }

    override fun deletePhoto(photoId: String) {
        launchMutation(InspectionActAction.DeletePhoto) {
            inspectionAct.deletePhoto(bookingId, type, photoId)
            inspectionAct.get(bookingId, type)
        }
    }

    override fun sign() {
        val ready = state.value as? InspectionActUiState.Ready ?: return
        val role =
            ready.viewerRole ?: run {
                showError("Нет доступа к подписанию акта", lastAct)
                return
            }
        launchMutation(InspectionActAction.Sign) {
            when (role) {
                InspectionActViewerRole.Owner -> signAsOwner(ready)
                InspectionActViewerRole.Renter -> signAsRenter(ready)
            }
        }
    }

    override fun downloadPdf() {
        if (!begin(InspectionActAction.DownloadPdf)) return
        coroutineScope.launch {
            try {
                _effects.emit(InspectionActEffect.OpenPdf(inspectionAct.download(bookingId, type)))
            } catch (exception: Throwable) {
                showError(errorMessage(exception, "Не удалось подготовить PDF"), lastAct)
            } finally {
                finish(InspectionActAction.DownloadPdf)
            }
        }
    }

    private suspend fun signAsOwner(ready: InspectionActUiState.Ready): BookingInspectionActDto {
        if (!ready.act.canCurrentUserEditMetrics(InspectionActViewerRole.Owner)) {
            return inspectionAct.signAsOwner(bookingId, type)
        }
        val fuel = ready.fuelInput.toIntOrNull()
        val mileage = ready.mileageInput.toIntOrNull()
        if (fuel == null || mileage == null) {
            error("Введите корректные значения топлива и пробега")
        }
        var act =
            inspectionAct.updateMetrics(
                bookingId = bookingId,
                type = type,
                request = UpdateInspectionMetricsRequest(fuel, mileage),
            )
        if (ready.act.canCurrentUserEditComment(InspectionActViewerRole.Owner)) {
            act =
                inspectionAct.updateComment(
                    bookingId = bookingId,
                    type = type,
                    request = commentRequest(ready.commentInput),
                )
        }
        return inspectionAct.signAsOwner(bookingId, type)
    }

    private suspend fun signAsRenter(ready: InspectionActUiState.Ready): BookingInspectionActDto {
        if (ready.act.canCurrentUserEditMetrics(InspectionActViewerRole.Renter)) {
            val fuel = ready.fuelInput.toIntOrNull()
            val mileage = ready.mileageInput.toIntOrNull()
            if (fuel == null || mileage == null) {
                error("Введите корректные значения топлива и пробега")
            }
            inspectionAct.updateMetrics(
                bookingId = bookingId,
                type = type,
                request = UpdateInspectionMetricsRequest(fuel, mileage),
            )
        }
        if (ready.act.canCurrentUserEditComment(InspectionActViewerRole.Renter)) {
            inspectionAct.updateComment(
                bookingId = bookingId,
                type = type,
                request = commentRequest(ready.commentInput),
            )
        }
        return inspectionAct.signAsRenter(bookingId, type)
    }

    private fun commentRequest(input: String): UpdateInspectionCommentRequest =
        UpdateInspectionCommentRequest(input.trim().takeIf { it.isNotEmpty() })

    private fun launchMutation(
        action: InspectionActAction,
        request: suspend () -> BookingInspectionActDto?,
    ) {
        if (!begin(action)) return
        coroutineScope.launch {
            try {
                request()?.let(::applyAct)
                _error.value = null
            } catch (exception: Throwable) {
                showError(errorMessage(exception, "Не удалось обновить акт"), lastAct)
            } finally {
                finish(action)
            }
        }
    }

    private fun applyAct(act: BookingInspectionActDto) {
        lastAct = act
        val role = viewerRole
        _state.value =
            InspectionActUiState.Ready(
                act = act,
                viewerRole = role,
                ownerId = ownerId,
                renterId = renterId,
                ownerName = ownerName,
                renterName = renterName,
                fuelInput = act.fuelRemaining?.toString().orEmpty(),
                mileageInput = act.mileage?.toString().orEmpty(),
                commentInput = role?.let { act.commentInputFor(it) }.orEmpty(),
            )
    }

    private fun updateReady(transform: (InspectionActUiState.Ready) -> InspectionActUiState.Ready) {
        val ready = state.value as? InspectionActUiState.Ready ?: return
        _state.value = transform(ready)
    }

    private fun begin(action: InspectionActAction): Boolean {
        var started = false
        _actionsInProgress.update { current ->
            if (action in current) {
                current
            } else {
                started = true
                current + action
            }
        }
        return started
    }

    private fun finish(action: InspectionActAction) {
        _actionsInProgress.update { it - action }
    }

    private fun showError(
        message: String,
        previousAct: BookingInspectionActDto?,
    ) {
        _error.value = message
        _state.value =
            InspectionActUiState.Error(
                message = message,
                previousAct = previousAct,
                viewerRole = viewerRole,
                ownerId = ownerId,
                renterId = renterId,
                ownerName = ownerName,
                renterName = renterName,
            )
    }

    private fun errorMessage(
        exception: Throwable,
        fallback: String,
    ): String = exception.message?.takeIf { it.isNotBlank() } ?: fallback
}
