package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingContractDownloadDto
import my.drivebit.utils.safeLaunchWithErrorHandler

sealed interface BookingContractUiState {
    data object Idle : BookingContractUiState

    data object Loading : BookingContractUiState

    data class Ready(
        val contract: BookingContractDownloadDto,
    ) : BookingContractUiState

    data class Error(
        val message: String,
    ) : BookingContractUiState
}

interface BookingContractViewModel {
    val uiState: StateFlow<BookingContractUiState>

    fun loadContract()
}

class BookingContractViewModelImpl(
    private val booking: Booking,
    private val bookingId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BookingContractViewModel {
    private val _uiState = MutableStateFlow<BookingContractUiState>(BookingContractUiState.Idle)
    override val uiState: StateFlow<BookingContractUiState> = _uiState.asStateFlow()

    override fun loadContract() {
        if (bookingId.isBlank()) {
            _uiState.value = BookingContractUiState.Error("Не указан номер бронирования")
            return
        }
        if (_uiState.value is BookingContractUiState.Loading) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _uiState.value is BookingContractUiState.Loading },
            setLoading = { loading ->
                if (loading) {
                    _uiState.value = BookingContractUiState.Loading
                }
            },
            setError = { message ->
                _uiState.value = BookingContractUiState.Error(message ?: "Не удалось загрузить договор")
            },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить договор",
                )
            },
        ) {
            val contract = booking.getContract(bookingId)
            _uiState.value = BookingContractUiState.Ready(contract)
        }
    }
}
