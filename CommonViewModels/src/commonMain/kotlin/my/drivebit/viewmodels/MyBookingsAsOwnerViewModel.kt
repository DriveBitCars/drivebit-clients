package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingDTO
import my.drivebit.utils.safeLaunchWithErrorHandler

interface MyBookingsAsOwnerViewModel {
    val bookings: StateFlow<List<BookingDTO>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val actionInProgress: StateFlow<Set<String>>

    fun loadBookings()

    fun refreshBookings()

    fun confirmBooking(bookingId: String)

    fun declineBooking(bookingId: String)

    fun signContractAsOwner(bookingId: String)
}

class MyBookingsAsOwnerViewModelImpl(
    private val booking: Booking,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyBookingsAsOwnerViewModel {
    private val _bookings = MutableStateFlow<List<BookingDTO>>(emptyList())
    override val bookings: StateFlow<List<BookingDTO>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _actionInProgress = MutableStateFlow<Set<String>>(emptySet())
    override val actionInProgress: StateFlow<Set<String>> = _actionInProgress.asStateFlow()

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
                    defaultGenericError = "Не удалось загрузить сделки",
                )
            },
        ) {
            val result = booking.getMyAsOwner()
            _bookings.value = result
        }
    }

    override fun refreshBookings() {
        coroutineScope.launch {
            runCatching {
                val result = booking.getMyAsOwner()
                _bookings.value = result
                _error.value = null
            }
        }
    }

    override fun confirmBooking(bookingId: String) {
        if (bookingId in _actionInProgress.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { false },
            setLoading = { },
            setError = { _error.value = it },
            errorHandler = { e ->
                logBookingActionError(
                    action = "confirmBooking",
                    bookingId = bookingId,
                    exception = e,
                )
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось подтвердить сделку",
                )
            },
        ) {
            _actionInProgress.update { it + bookingId }
            try {
                booking.confirmAsOwner(bookingId)
                val result = booking.getMyAsOwner()
                _bookings.value = result
            } finally {
                _actionInProgress.update { it - bookingId }
            }
        }
    }

    override fun declineBooking(bookingId: String) {
        if (bookingId in _actionInProgress.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { false },
            setLoading = { },
            setError = { _error.value = it },
            errorHandler = { e ->
                logBookingActionError(
                    action = "declineBooking",
                    bookingId = bookingId,
                    exception = e,
                )
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось отклонить сделку",
                )
            },
        ) {
            _actionInProgress.update { it + bookingId }
            try {
                booking.declineAsOwner(bookingId)
                val result = booking.getMyAsOwner()
                _bookings.value = result
            } finally {
                _actionInProgress.update { it - bookingId }
            }
        }
    }

    override fun signContractAsOwner(bookingId: String) {
        if (bookingId in _actionInProgress.value) return

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { false },
            setLoading = { },
            setError = { _error.value = it },
            errorHandler = { e ->
                logBookingActionError(
                    action = "signContractAsOwner",
                    bookingId = bookingId,
                    exception = e,
                )
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось подписать договор",
                )
            },
        ) {
            _actionInProgress.update { it + bookingId }
            try {
                val updated = booking.signContractAsOwner(bookingId)
                println(
                    "✅ [MyBookingsAsOwnerViewModel] signContractAsOwner succeeded bookingId=$bookingId status=${updated.status}",
                )
                _error.value = null
                _bookings.update { list ->
                    list.map { if (it.id == bookingId) updated else it }
                }
            } finally {
                _actionInProgress.update { it - bookingId }
            }
        }
    }
}

private fun logBookingActionError(
    action: String,
    bookingId: String,
    exception: Throwable,
) {
    println("❌ [MyBookingsAsOwnerViewModel] $action failed bookingId=$bookingId")
    println("   exception: ${exception::class.simpleName}")
    println("   message: ${exception.message}")
    if (exception is NetworkException) {
        println("   httpStatus: ${exception.statusCodeValue}")
    }
    exception.printStackTrace()
}
