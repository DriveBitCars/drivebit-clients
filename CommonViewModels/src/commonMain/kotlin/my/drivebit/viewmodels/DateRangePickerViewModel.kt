package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DateRangePickerState(
    val isCalendarOpen: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null,
)

class DateRangePickerViewModel(
    initialStartDate: String? = null,
    initialEndDate: String? = null,
) {
    private val _state =
        MutableStateFlow(
            DateRangePickerState(
                isCalendarOpen = false,
                startDate = initialStartDate,
                endDate = initialEndDate,
            ),
        )

    val state: StateFlow<DateRangePickerState>
        get() = _state.asStateFlow()

    fun openCalendar() {
        _state.update { it.copy(isCalendarOpen = true) }
    }

    fun closeCalendar() {
        _state.update { it.copy(isCalendarOpen = false) }
    }

    fun setStartDate(date: String?) {
        _state.update { currentState ->
            val newStartDate = date
            val newEndDate =
                if (newStartDate != null && currentState.endDate != null && newStartDate > currentState.endDate) {
                    null
                } else {
                    currentState.endDate
                }
            currentState.copy(
                startDate = newStartDate,
                endDate = newEndDate,
            )
        }
    }

    fun setEndDate(date: String?) {
        _state.update { currentState ->
            val newEndDate = date
            if (currentState.startDate == null || newEndDate == null || newEndDate >= currentState.startDate) {
                currentState.copy(endDate = newEndDate)
            } else {
                currentState
            }
        }
    }

    fun clearDates() {
        _state.update {
            it.copy(
                startDate = null,
                endDate = null,
            )
        }
    }
}
