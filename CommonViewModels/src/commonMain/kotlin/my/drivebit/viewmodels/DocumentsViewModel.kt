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
}
