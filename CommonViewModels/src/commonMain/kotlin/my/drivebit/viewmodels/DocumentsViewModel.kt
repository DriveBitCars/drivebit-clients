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

sealed interface DocumentsState {
    data object Idle : DocumentsState

    data object Loading : DocumentsState

    data class Uploading(
        val documentType: String,
        val documents: List<Document>,
    ) : DocumentsState

    data class Success(
        val documents: List<Document>,
    ) : DocumentsState

    data class Error(
        val message: String,
    ) : DocumentsState
}

interface DocumentsViewModel {
    val state: StateFlow<DocumentsState>

    fun load()

    fun uploadDocument(
        documentType: String,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    )
}

class DocumentsViewModelImpl(
    private val documents: Documents,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : DocumentsViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<DocumentsState>(DocumentsState.Idle)

    override val state: StateFlow<DocumentsState>
        get() = _state.asStateFlow()

    override fun load() {
        viewModelScope.launch {
            _state.update { DocumentsState.Loading }

            runCatching {
                documents.getDocuments()
            }.onSuccess { docs ->
                _state.update { DocumentsState.Success(docs) }
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить документы",
                    )
                _state.update { DocumentsState.Error(message) }
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
            _state.update { DocumentsState.Uploading(documentType, currentDocs) }

            runCatching {
                findLatestDocumentByType(currentDocs, documentType)?.let { existing ->
                    documents.deleteDocument(existing.id)
                }
                documents.uploadDocument(
                    fileBytes = fileBytes,
                    fileName = fileName,
                    contentType = contentType,
                    documentType = documentType,
                )
            }.onSuccess {
                load()
            }.onFailure { e ->
                val message =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить документ",
                    )
                _state.update { DocumentsState.Error(message) }
            }
        }
    }

    private fun currentDocumentsList(): List<Document> =
        when (val current = _state.value) {
            is DocumentsState.Success -> current.documents
            is DocumentsState.Uploading -> current.documents
            else -> emptyList()
        }

    private fun findLatestDocumentByType(
        documents: List<Document>,
        type: String,
    ): Document? =
        documents
            .filter { it.type == type }
            .maxByOrNull { it.uploadDate ?: "" }
}
