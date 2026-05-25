package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Car
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents

object CarStsDocumentTypes {
    const val FRONT = "VehicleRegistrationFrontRus"
    const val BACK = "VehicleRegistrationBackRus"

    val all = setOf(FRONT, BACK)
}

sealed interface CarStsDocumentsState {
    data object Idle : CarStsDocumentsState

    data object Loading : CarStsDocumentsState

    data class Uploading(
        val documentType: String,
        val documents: List<Document>,
        val stsSeriesNumber: String?,
    ) : CarStsDocumentsState

    data class Success(
        val documents: List<Document>,
        val stsSeriesNumber: String?,
    ) : CarStsDocumentsState

    data class Error(
        val message: String,
    ) : CarStsDocumentsState
}

interface CarStsDocumentsViewModel {
    val state: StateFlow<CarStsDocumentsState>

    fun load()

    fun uploadDocument(
        documentType: String,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    )
}

class CarStsDocumentsViewModelImpl(
    private val carId: String,
    private val documents: Documents,
    private val car: Car,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarStsDocumentsViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<CarStsDocumentsState>(CarStsDocumentsState.Idle)

    override val state: StateFlow<CarStsDocumentsState>
        get() = _state.asStateFlow()

    override fun load() {
        viewModelScope.launch {
            _state.update { CarStsDocumentsState.Loading }

            runCatching {
                val allDocuments = documents.getDocuments()
                val myCarCount = runCatching { car.getMyCars().size }.getOrDefault(1)
                val carDocuments = filterStsDocumentsForCar(allDocuments, carId, myCarCount)
                val stsSeriesNumber = loadStsSeriesNumber()
                carDocuments to stsSeriesNumber
            }.onSuccess { (carDocuments, stsSeriesNumber) ->
                _state.update {
                    CarStsDocumentsState.Success(
                        documents = carDocuments,
                        stsSeriesNumber = stsSeriesNumber,
                    )
                }
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить документы СТС",
                    )
                _state.update { CarStsDocumentsState.Error(message) }
            }
        }
    }

    override fun uploadDocument(
        documentType: String,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ) {
        viewModelScope.launch {
            val currentDocs = currentDocumentsList()
            val stsSeriesNumber = currentStsSeriesNumber()
            _state.update {
                CarStsDocumentsState.Uploading(
                    documentType = documentType,
                    documents = currentDocs,
                    stsSeriesNumber = stsSeriesNumber,
                )
            }

            runCatching {
                findLatestDocumentByType(currentDocs, documentType)?.let { existing ->
                    documents.deleteDocument(existing.id)
                }
                documents.uploadDocument(
                    fileBytes = fileBytes,
                    fileName = fileName,
                    contentType = contentType,
                    documentType = documentType,
                    carId = carId,
                )
            }.onSuccess { uploaded ->
                applyUploadedDocument(uploaded, documentType)
                load()
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить документ",
                    )
                _state.update { CarStsDocumentsState.Error(message) }
            }
        }
    }

    private suspend fun loadStsSeriesNumber(): String? =
        runCatching {
            car
                .getCar(carId)
                .stsSeriesNumber
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull()

    private fun currentDocumentsList(): List<Document> =
        when (val current = _state.value) {
            is CarStsDocumentsState.Success -> current.documents
            is CarStsDocumentsState.Uploading -> current.documents
            else -> emptyList()
        }

    private fun currentStsSeriesNumber(): String? =
        when (val current = _state.value) {
            is CarStsDocumentsState.Success -> current.stsSeriesNumber
            is CarStsDocumentsState.Uploading -> current.stsSeriesNumber
            else -> null
        }

    private fun findLatestDocumentByType(
        documents: List<Document>,
        type: String,
    ): Document? =
        documents
            .filter { it.type == type }
            .maxByOrNull { it.uploadDate ?: "" }

    private fun applyUploadedDocument(
        uploaded: Document,
        documentType: String,
    ) {
        val withCarId =
            if (uploaded.carId.isNullOrBlank()) {
                uploaded.copy(carId = carId)
            } else {
                uploaded
            }
        val merged =
            currentDocumentsList()
                .filter { it.type != documentType }
                .plus(withCarId)
        val stsSeriesNumber = currentStsSeriesNumber()
        _state.update {
            CarStsDocumentsState.Success(
                documents = merged,
                stsSeriesNumber = stsSeriesNumber,
            )
        }
    }

    companion object {
        fun filterStsDocumentsForCar(
            documents: List<Document>,
            carId: String,
            myCarCount: Int,
        ): List<Document> =
            CarStsDocumentTypes.all.mapNotNull { type ->
                resolveStsDocumentForCar(
                    documents = documents,
                    carId = carId,
                    type = type,
                    allowOrphanWithoutCarId = myCarCount <= 1,
                )
            }

        private fun resolveStsDocumentForCar(
            documents: List<Document>,
            carId: String,
            type: String,
            allowOrphanWithoutCarId: Boolean,
        ): Document? {
            val active =
                documents.filter { doc ->
                    doc.type == type && doc.status != "Expired"
                }
            return active.firstOrNull { it.carId?.equals(carId, ignoreCase = true) == true }
                ?: if (allowOrphanWithoutCarId) {
                    active
                        .filter { it.carId.isNullOrBlank() }
                        .maxByOrNull { it.uploadDate ?: "" }
                } else {
                    null
                }
        }
    }
}
