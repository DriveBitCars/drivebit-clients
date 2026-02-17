package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingDTO
import my.drivebit.utils.safeLaunchWithErrorHandler

interface MyBookingsAsRenterViewModel {
    val bookings: StateFlow<List<BookingDTO>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun loadBookings()
}

class MyBookingsAsRenterViewModelImpl(
    private val booking: Booking,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyBookingsAsRenterViewModel {
    private val _bookings = MutableStateFlow<List<BookingDTO>>(emptyList())
    override val bookings: StateFlow<List<BookingDTO>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override fun loadBookings() {
        if (_isLoading.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isLoading.value },
            setLoading = { _isLoading.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить бронирования",
                )
            },
        ) {
            val result = booking.getMyAsRenter()
            _bookings.value = result
        }
    }
}
