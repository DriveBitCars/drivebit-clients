package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingContractDownloadDto
import my.drivebit.network.services.GetBookingContractResult
import my.drivebit.utils.safeLaunchWithErrorHandler

sealed interface BookingContractUiState {
    data object Idle : BookingContractUiState

    data object Loading : BookingContractUiState

    data class Ready(
        val contract: BookingContractDownloadDto,
    ) : BookingContractUiState

    data class Error(
        val message: String,
        val reasons: List<String> = emptyList(),
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
                    println("📄 [BookingContractVM] state=Loading bookingId=$bookingId")
                }
            },
            setError = { message ->
                if (message != null) {
                    _uiState.value = BookingContractUiState.Error(message)
                    println("📄 [BookingContractVM] state=Error message=$message bookingId=$bookingId")
                }
            },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить договор",
                )
            },
        ) {
            when (val result = booking.getContract(bookingId)) {
                is GetBookingContractResult.Success -> {
                    _uiState.value = BookingContractUiState.Ready(result.contract)
                    println(
                        "📄 [BookingContractVM] state=Ready bookingId=$bookingId " +
                            "contractNumber=${result.contract.contractNumber} file=${result.contract.fileName}",
                    )
                }
                is GetBookingContractResult.DataIncomplete -> {
                    _uiState.value =
                        BookingContractUiState.Error(
                            message = result.message,
                            reasons = result.reasons,
                        )
                    println(
                        "📄 [BookingContractVM] state=DataIncomplete bookingId=$bookingId " +
                            "message=${result.message} reasons=${result.reasons}",
                    )
                }
                is GetBookingContractResult.Failed -> {
                    _uiState.value = BookingContractUiState.Error(message = result.message)
                    println(
                        "📄 [BookingContractVM] state=Failed bookingId=$bookingId message=${result.message}",
                    )
                }
            }
        }
    }
}
