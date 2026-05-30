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
import kotlinx.coroutines.launch
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingCheckoutKind
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment
import my.drivebit.network.services.checkoutBooking
import my.drivebit.utils.safeLaunchWithErrorHandler

interface MyBookingsAsRenterViewModel {
    val bookings: StateFlow<List<BookingDTO>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val isPaying: StateFlow<Boolean>
    val payEffects: SharedFlow<ChatPayEffect>

    fun loadBookings()

    fun payBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    )

    fun prepayBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    )
}

class MyBookingsAsRenterViewModelImpl(
    private val booking: Booking,
    private val payment: Payment,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyBookingsAsRenterViewModel {
    private val _bookings = MutableStateFlow<List<BookingDTO>>(emptyList())
    override val bookings: StateFlow<List<BookingDTO>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _isPaying = MutableStateFlow(false)
    override val isPaying: StateFlow<Boolean> = _isPaying.asStateFlow()

    private val _payEffects = MutableSharedFlow<ChatPayEffect>(extraBufferCapacity = 1)
    override val payEffects: SharedFlow<ChatPayEffect> = _payEffects.asSharedFlow()

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

    override fun payBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ) {
        checkoutBooking(
            bookingId = bookingId,
            kind = BookingCheckoutKind.FullOrBalance,
            returnUrl = returnUrl,
            failUrl = failUrl,
        )
    }

    override fun prepayBooking(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ) {
        checkoutBooking(
            bookingId = bookingId,
            kind = BookingCheckoutKind.Prepayment,
            returnUrl = returnUrl,
            failUrl = failUrl,
        )
    }

    private fun checkoutBooking(
        bookingId: String,
        kind: BookingCheckoutKind,
        returnUrl: String,
        failUrl: String,
    ) {
        if (bookingId.isBlank() || _isPaying.value) return
        coroutineScope.launch {
            _isPaying.value = true
            try {
                when (val result = payment.checkoutBooking(bookingId, kind, returnUrl, failUrl)) {
                    is PayBookingResult.Redirect ->
                        _payEffects.emit(ChatPayEffect.OpenCheckout(result.url))
                    is PayBookingResult.AlreadyPaid -> {
                        val text =
                            result.message?.takeIf { it.isNotBlank() }
                                ?: "Оплата уже выполнена"
                        _payEffects.emit(ChatPayEffect.ShowInfo(text))
                        loadBookings()
                    }
                    is PayBookingResult.Failed ->
                        _payEffects.emit(ChatPayEffect.ShowInfo(result.message))
                }
            } finally {
                _isPaying.value = false
            }
        }
    }
}
