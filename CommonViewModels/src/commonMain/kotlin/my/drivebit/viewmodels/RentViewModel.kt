package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingCheckoutKind
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CreateBookingRequest
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment
import my.drivebit.network.services.isTerminalRenterBooking
import my.drivebit.network.services.prepaymentButtonLabel
import my.drivebit.network.services.renterFullOrBalanceAmountRub
import my.drivebit.network.services.renterFullOrBalancePaymentLabel
import my.drivebit.network.services.statusAllowsRenterPayment
import my.drivebit.shared.storage.Storage

sealed interface RentPayEffect {
    data class OpenCheckout(
        val url: String,
        val bookingId: String = "",
    ) : RentPayEffect

    data class ShowInfo(
        val text: String,
        val bookingId: String = "",
        val alreadyPaid: Boolean = false,
    ) : RentPayEffect
}

sealed interface PendingBookingPaymentUi {
    data class AwaitingOwnerConfirmation(
        val statusLabel: String,
    ) : PendingBookingPaymentUi

    data class ReadyToPay(
        val canPayPrepayment: Boolean = false,
        val prepaymentButtonLabel: String = "Предоплата",
        val fullPaymentLabel: String = "Оплатить",
        val fullOrBalanceAmountRub: Int = 0,
    ) : PendingBookingPaymentUi
}

sealed interface RentState {
    data class Book(
        val startDate: String? = null,
        val showStartDateError: Boolean = false,
        val endDate: String? = null,
        val showEndDateError: Boolean = false,
        val middlePrice: String = "",
        val totalAmount: String = "",
        val depositAmount: String = "",
        val isCreating: Boolean = false,
        val createError: String? = null,
        val pendingPaymentBookingId: String? = null,
        val pendingBookingPaymentUi: PendingBookingPaymentUi? = null,
    ) : RentState

    data object NavigateToMyBookings : RentState

    data class NavigateToLogin(
        val carId: String,
        val startDate: String? = null,
        val endDate: String? = null,
    ) : RentState
}

interface RentViewModel {
    val state: StateFlow<RentState>
    val isPaying: StateFlow<Boolean>
    val payEffects: SharedFlow<RentPayEffect>

    fun setStartDate(date: String?)

    fun setEndDate(date: String?)

    fun setInitialDates(
        startAt: String?,
        endAt: String?,
    )

    fun consumeNavigationEvent()

    fun onBookClick()

    fun tryConsumeAutoBookAfterLogin(): Boolean

    fun payCreatedBooking(
        kind: BookingCheckoutKind,
        returnUrl: String,
        failUrl: String,
    )

    fun requestNavigateToMyBookings()

    fun refreshPendingBookingForCurrentSelection()
}

class RentViewModelImpl(
    private val booking: Booking,
    private val payment: Payment,
    private val storage: Storage,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : RentViewModel {
    private val viewModelScope = coroutineScope
    private var calculateJob: Job? = null
    private var createJob: Job? = null
    private var autoBookAfterLoginConsumed: Boolean = false

    private val _state = MutableStateFlow<RentState>(RentState.Book())
    override val state: StateFlow<RentState> = _state.asStateFlow()

    private val _isPaying = MutableStateFlow(false)
    override val isPaying: StateFlow<Boolean> = _isPaying.asStateFlow()

    private val _payEffects = MutableSharedFlow<RentPayEffect>(extraBufferCapacity = 1)
    override val payEffects: SharedFlow<RentPayEffect> = _payEffects.asSharedFlow()

    override fun tryConsumeAutoBookAfterLogin(): Boolean {
        if (autoBookAfterLoginConsumed) return false
        autoBookAfterLoginConsumed = true
        return true
    }

    override fun consumeNavigationEvent() {
        _state.value = RentState.Book()
    }

    override fun setStartDate(date: String?) {
        _state.update {
            if (it is RentState.Book) {
                if (sameBookingInstant(it.startDate, date)) {
                    return@update it.copy(showStartDateError = false)
                }
                val currentEnd = it.endDate
                val endDate =
                    when {
                        date == null -> null
                        currentEnd != null -> {
                            runCatching {
                                val startInstant = Instant.parse(date)
                                val endInstant = Instant.parse(currentEnd)
                                if (endInstant <= startInstant) null else currentEnd
                            }.getOrElse { currentEnd }
                        }
                        else -> currentEnd
                    }
                it.copy(
                    startDate = date,
                    endDate = endDate,
                    showStartDateError = false,
                    pendingPaymentBookingId = null,
                    pendingBookingPaymentUi = null,
                )
            } else {
                it
            }
        }
        scheduleCalculate()
    }

    override fun setEndDate(date: String?) {
        _state.update {
            if (it is RentState.Book) {
                if (sameBookingInstant(it.endDate, date)) {
                    return@update it.copy(showEndDateError = false)
                }
                it.copy(
                    endDate = date,
                    showEndDateError = false,
                    pendingPaymentBookingId = null,
                    pendingBookingPaymentUi = null,
                )
            } else {
                it
            }
        }
        scheduleCalculate()
    }

    override fun setInitialDates(
        startAt: String?,
        endAt: String?,
    ) {
        val startTrimmed = startAt?.takeIf { it.isNotBlank() }
        val endDateValid =
            when {
                startTrimmed == null || endAt.isNullOrBlank() -> endAt?.takeIf { it.isNotBlank() }
                else ->
                    runCatching {
                        val startInstant = Instant.parse(startTrimmed)
                        val endInstant = Instant.parse(endAt)
                        if (endInstant <= startInstant) null else endAt
                    }.getOrElse { endAt }
            }
        _state.update {
            if (it is RentState.Book) {
                if (sameBookingInstant(it.startDate, startTrimmed) &&
                    sameBookingInstant(it.endDate, endDateValid)
                ) {
                    return@update it.copy(
                        showStartDateError = false,
                        showEndDateError = false,
                    )
                }
                it.copy(
                    startDate = startTrimmed,
                    endDate = endDateValid,
                    showStartDateError = false,
                    showEndDateError = false,
                    pendingPaymentBookingId = null,
                    pendingBookingPaymentUi = null,
                )
            } else {
                it
            }
        }
        if (startTrimmed != null && endDateValid != null) {
            scheduleCalculateWith(startTrimmed, endDateValid)
        } else {
            scheduleCalculate()
        }
    }

    override fun onBookClick() {
        val current = _state.value as? RentState.Book ?: return
        if (current.pendingPaymentBookingId != null) return
        if (current.isCreating) return
        if (!storage.isLogined()) {
            _state.value =
                RentState.NavigateToLogin(
                    carId = carId,
                    startDate = current.startDate,
                    endDate = current.endDate,
                )
            return
        }
        val showStartDateError = current.startDate.isNullOrBlank()
        val endDateBlank = current.endDate.isNullOrBlank()
        val endDateTooEarly =
            !endDateBlank &&
                current.startDate != null &&
                runCatching {
                    Instant.parse(current.endDate!!) <= Instant.parse(current.startDate!!)
                }.getOrDefault(true)
        val showEndDateError = endDateBlank || endDateTooEarly
        _state.update {
            if (it is RentState.Book) {
                it.copy(
                    showStartDateError = showStartDateError,
                    showEndDateError = showEndDateError,
                    createError = null,
                )
            } else {
                it
            }
        }
        if (showStartDateError || showEndDateError) return
        val start = current.startDate!!.trim()
        val end = current.endDate!!.trim()
        createJob?.cancel()
        _state.update {
            if (it is RentState.Book) it.copy(isCreating = true, createError = null) else it
        }
        createJob =
            viewModelScope.launch {
                runCatching {
                    booking.createAsRenter(
                        CreateBookingRequest(
                            carId = carId,
                            startAt = start,
                            endAt = end,
                        ),
                    )
                }.onSuccess { dto ->
                    _state.update { s ->
                        if (s is RentState.Book) {
                            s.copy(
                                isCreating = false,
                                createError = null,
                                pendingPaymentBookingId = dto.id,
                                pendingBookingPaymentUi = pendingBookingPaymentUiFromDto(dto),
                            )
                        } else {
                            s
                        }
                    }
                }.onFailure { e ->
                    if (isUnauthorizedBookingError(e)) {
                        storage.logout()
                        val bookState = _state.value as? RentState.Book ?: current
                        _state.value =
                            RentState.NavigateToLogin(
                                carId = carId,
                                startDate = bookState.startDate ?: start,
                                endDate = bookState.endDate ?: end,
                            )
                    } else {
                        _state.update { s ->
                            if (s is RentState.Book) {
                                s.copy(
                                    isCreating = false,
                                    createError = localizeBookingError(e.message),
                                )
                            } else {
                                s
                            }
                        }
                    }
                }
            }
    }

    private fun scheduleCalculateWith(
        startAt: String,
        endAt: String,
    ) {
        calculateJob?.cancel()
        calculateJob =
            viewModelScope.launch {
                runCatching {
                    booking.calculate(
                        CheckBookingAvailabilityRequest(
                            carId = carId,
                            startAt = startAt,
                            endAt = endAt,
                        ),
                    )
                }.onSuccess { response ->
                    val total = response.totalPrice ?: response.estimatedPrice ?: 0.0
                    val days = daysBetween(startAt, endAt).coerceAtLeast(1)
                    val pricePerDayFromApi = response.pricePerDay
                    val middle =
                        when {
                            pricePerDayFromApi != null && pricePerDayFromApi > 0 -> pricePerDayFromApi
                            days > 0 -> total / days
                            else -> 0.0
                        }
                    val deposit = response.estimatedDeposit ?: 0.0
                    _state.update {
                        if (it is RentState.Book) {
                            it.copy(
                                totalAmount = formatPrice(total),
                                middlePrice = formatPrice(middle),
                                depositAmount = if (deposit > 0) formatPrice(deposit) else "",
                            )
                        } else {
                            it
                        }
                    }
                }.onFailure {
                    _state.update { current ->
                        if (current is RentState.Book) {
                            current.copy(totalAmount = "", middlePrice = "", depositAmount = "")
                        } else {
                            current
                        }
                    }
                }
            }
    }

    private fun scheduleCalculate() {
        calculateJob?.cancel()
        calculateJob =
            viewModelScope.launch {
                val current = _state.value as? RentState.Book ?: return@launch
                val start = current.startDate?.takeIf { it.isNotBlank() }
                val end = current.endDate?.takeIf { it.isNotBlank() }
                if (start == null || end == null) {
                    _state.update {
                        if (it is RentState.Book) {
                            it.copy(
                                totalAmount = "",
                                middlePrice = "",
                                depositAmount = "",
                            )
                        } else {
                            it
                        }
                    }
                    return@launch
                }
                runCatching {
                    booking.calculate(
                        CheckBookingAvailabilityRequest(
                            carId = carId,
                            startAt = start,
                            endAt = end,
                        ),
                    )
                }.onSuccess { response ->
                    val total = response.totalPrice ?: response.estimatedPrice ?: 0.0
                    val days = daysBetween(start, end).coerceAtLeast(1)
                    val pricePerDayFromApi = response.pricePerDay
                    val middle =
                        when {
                            pricePerDayFromApi != null && pricePerDayFromApi > 0 -> pricePerDayFromApi
                            days > 0 -> total / days
                            else -> 0.0
                        }
                    val deposit = response.estimatedDeposit ?: 0.0
                    _state.update {
                        if (it is RentState.Book) {
                            it.copy(
                                totalAmount = formatPrice(total),
                                middlePrice = formatPrice(middle),
                                depositAmount = if (deposit > 0) formatPrice(deposit) else "",
                            )
                        } else {
                            it
                        }
                    }
                }.onFailure {
                    _state.update { current ->
                        if (current is RentState.Book) {
                            current.copy(totalAmount = "", middlePrice = "", depositAmount = "")
                        } else {
                            current
                        }
                    }
                }
            }
    }

    private fun isUnauthorizedBookingError(exception: Throwable): Boolean {
        val networkException = exception as? NetworkException
        if (networkException?.statusCodeValue == 401) {
            return true
        }
        val message = exception.message?.lowercase().orEmpty()
        return message.contains("unauthorized") ||
            message.contains("401") ||
            message.contains("invalid refresh token")
    }

    private fun sameBookingInstant(
        left: String?,
        right: String?,
    ): Boolean {
        if (left == right) return true
        if (left.isNullOrBlank() || right.isNullOrBlank()) return false
        return runCatching {
            Instant.parse(left) == Instant.parse(right)
        }.getOrDefault(false)
    }

    private fun localizeBookingError(message: String?): String =
        when {
            message == null || message.isBlank() -> "Не удалось создать бронирование"
            "CannotBookOwnCar" in message -> "Нельзя забронировать свой автомобиль"
            "Start date cannot be in the past" in message -> "Дата начала не может быть в прошлом"
            else -> message
        }

    private fun formatPrice(value: Double): String =
        kotlin.math
            .round(value)
            .toInt()
            .toString()

    private fun daysBetween(
        startAt: String,
        endAt: String,
    ): Int =
        runCatching {
            val start = Instant.parse(startAt)
            val end = Instant.parse(endAt)
            (end - start).inWholeDays.toInt().coerceAtLeast(1)
        }.getOrElse { 1 }

    override fun payCreatedBooking(
        kind: BookingCheckoutKind,
        returnUrl: String,
        failUrl: String,
    ) {
        val current = _state.value as? RentState.Book ?: return
        if (current.pendingBookingPaymentUi !is PendingBookingPaymentUi.ReadyToPay) return
        val bookingId = current.pendingPaymentBookingId ?: return
        if (bookingId.isBlank() || _isPaying.value) return
        viewModelScope.launch {
            _isPaying.value = true
            try {
                when (
                    val result =
                        when (kind) {
                            BookingCheckoutKind.Prepayment ->
                                payment.registerBookingPrepayment(bookingId, returnUrl, failUrl)
                            BookingCheckoutKind.FullOrBalance ->
                                payment.registerBookingPayment(bookingId, returnUrl, failUrl)
                        }
                ) {
                    is PayBookingResult.Redirect ->
                        _payEffects.emit(RentPayEffect.OpenCheckout(result.url, bookingId = bookingId))
                    is PayBookingResult.AlreadyPaid -> {
                        val text =
                            result.message?.takeIf { it.isNotBlank() }
                                ?: "Оплата уже выполнена"
                        _payEffects.emit(
                            RentPayEffect.ShowInfo(
                                text = text,
                                bookingId = bookingId,
                                alreadyPaid = true,
                            ),
                        )
                    }
                    is PayBookingResult.Failed ->
                        _payEffects.emit(
                            RentPayEffect.ShowInfo(
                                text = result.message,
                                bookingId = bookingId,
                                alreadyPaid = false,
                            ),
                        )
                }
            } finally {
                _isPaying.value = false
            }
        }
    }

    override fun requestNavigateToMyBookings() {
        _state.value = RentState.NavigateToMyBookings
    }

    override fun refreshPendingBookingForCurrentSelection() {
        val current = _state.value as? RentState.Book ?: return
        val start = current.startDate?.takeIf { it.isNotBlank() } ?: return
        val end = current.endDate?.takeIf { it.isNotBlank() } ?: return
        if (!storage.isLogined()) return
        viewModelScope.launch {
            val list =
                runCatching { booking.getMyAsRenter() }
                    .getOrElse { return@launch }
            val byId =
                current.pendingPaymentBookingId?.let { pendingId ->
                    list.find { it.id == pendingId }
                }
            val match =
                byId
                    ?: list.find { b ->
                        b.carId == carId &&
                            sameInstant(b.startAt, start) &&
                            sameInstant(b.endAt, end) &&
                            !b.isTerminalRenterBooking()
                    }
            _state.update { s ->
                if (s !is RentState.Book) return@update s
                when {
                    match == null -> s
                    match.isTerminalRenterBooking() ->
                        s.copy(
                            pendingPaymentBookingId = null,
                            pendingBookingPaymentUi = null,
                        )
                    else ->
                        s.copy(
                            pendingPaymentBookingId = match.id,
                            pendingBookingPaymentUi = pendingBookingPaymentUiFromDto(match),
                        )
                }
            }
        }
    }

    private fun pendingBookingPaymentUiFromDto(dto: BookingDTO): PendingBookingPaymentUi =
        when {
            dto.statusAllowsRenterPayment() ->
                PendingBookingPaymentUi.ReadyToPay(
                    canPayPrepayment = dto.canPayPrepayment,
                    prepaymentButtonLabel = dto.prepaymentButtonLabel(),
                    fullPaymentLabel =
                        "${dto.renterFullOrBalancePaymentLabel()} (${dto.renterFullOrBalanceAmountRub()} ₽)",
                    fullOrBalanceAmountRub = dto.renterFullOrBalanceAmountRub(),
                )
            else ->
                PendingBookingPaymentUi.AwaitingOwnerConfirmation(
                    dto.statusTranslate?.takeIf { it.isNotBlank() } ?: dto.status,
                )
        }

    private fun sameInstant(
        a: String,
        b: String,
    ): Boolean =
        runCatching {
            Instant.parse(a.trim()) == Instant.parse(b.trim())
        }.getOrDefault(false)
}
