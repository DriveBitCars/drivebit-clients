package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import my.drivebit.network.services.Booking
import my.drivebit.shared.storage.Storage
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CreateBookingRequest

sealed interface RentState {
    data class Book(
        val startDate: String? = null,
        val showStartDateError: Boolean = false,
        val endDate: String? = null,
        val showEndDateError: Boolean = false,
        val middlePrice: String = "",
        val totalAmount: String = "",
        val isCreating: Boolean = false,
        val createError: String? = null,
    ) : RentState

    data object NavigateToMyBookings : RentState

    data class NavigateToLogin(val carId: String) : RentState
}

interface RentViewModel {
    val state: StateFlow<RentState>

    fun setStartDate(date: String?)

    fun setEndDate(date: String?)

    fun consumeNavigationEvent()

    fun onBookClick()
}

class RentViewModelImpl(
    private val booking: Booking,
    private val storage: Storage,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : RentViewModel {
    private val viewModelScope = coroutineScope
    private var calculateJob: Job? = null
    private var createJob: Job? = null

    private val _state = MutableStateFlow<RentState>(RentState.Book())
    override val state: StateFlow<RentState> = _state.asStateFlow()

    override fun consumeNavigationEvent() {
        _state.value = RentState.Book()
    }

    override fun setStartDate(date: String?) {
        _state.update {
            if (it is RentState.Book) {
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
                it.copy(startDate = date, endDate = endDate, showStartDateError = false)
            } else {
                it
            }
        }
        scheduleCalculate()
    }

    override fun setEndDate(date: String?) {
        _state.update {
            if (it is RentState.Book) {
                it.copy(endDate = date, showEndDateError = false)
            } else {
                it
            }
        }
        scheduleCalculate()
    }

    override fun onBookClick() {
        if (!storage.isLogined()) {
            _state.value = RentState.NavigateToLogin(carId)
            return
        }
        val current = _state.value as? RentState.Book ?: return
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
        createJob =
            viewModelScope.launch {
                _state.update {
                    if (it is RentState.Book) it.copy(isCreating = true, createError = null) else it
                }
                runCatching {
                    booking.createAsRenter(
                        CreateBookingRequest(
                            carId = carId,
                            startAt = start,
                            endAt = end,
                        ),
                    )
                }.onSuccess {
                    _state.value = RentState.NavigateToMyBookings
                }.onFailure { e ->
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

    private fun scheduleCalculate() {
        calculateJob?.cancel()
        calculateJob =
            viewModelScope.launch {
                val current = _state.value as? RentState.Book ?: return@launch
                val start = current.startDate?.takeIf { it.isNotBlank() }
                val end = current.endDate?.takeIf { it.isNotBlank() }
                if (start == null || end == null) {
                    _state.update {
                        if (it is RentState.Book) it.copy(totalAmount = "", middlePrice = "") else it
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
                    val total = response.estimatedPrice ?: 0.0
                    val days = daysBetween(start, end).coerceAtLeast(1)
                    val middle = if (days > 0) total / days else 0.0
                    _state.update {
                        if (it is RentState.Book) {
                            it.copy(
                                totalAmount = formatPrice(total),
                                middlePrice = formatPrice(middle),
                            )
                        } else {
                            it
                        }
                    }
                }.onFailure {
                    _state.update { current ->
                        if (current is RentState.Book) {
                            current.copy(totalAmount = "", middlePrice = "")
                        } else {
                            current
                        }
                    }
                }
            }
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
}
