package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DateTimeFieldState(
    val date: String? = null,
    val time: String? = null,
    val isCalendarOpen: Boolean = false,
    val isTimePickerOpen: Boolean = false,
) {
    val dateOrNull: String? get() = date?.takeIf { it.length >= 10 }
    val timeOrNull: String? get() = time?.takeIf { it.matches(Regex("\\d{1,2}:\\d{2}")) }
}

class DateTimeFieldViewModel(
    initialDate: String? = null,
    initialTime: String? = null,
) {
    private val _state =
        MutableStateFlow(
            DateTimeFieldState(
                date = initialDate,
                time = initialTime,
                isCalendarOpen = false,
                isTimePickerOpen = false,
            ),
        )

    val state: StateFlow<DateTimeFieldState>
        get() = _state.asStateFlow()

    fun openCalendar() {
        _state.update { it.copy(isCalendarOpen = true) }
    }

    fun closeCalendar() {
        _state.update { it.copy(isCalendarOpen = false) }
    }

    fun openTimePicker() {
        _state.update { it.copy(isTimePickerOpen = true) }
    }

    fun closeTimePicker() {
        _state.update { it.copy(isTimePickerOpen = false) }
    }

    fun setDate(date: String?) {
        _state.update { it.copy(date = date) }
    }

    fun setTime(time: String?) {
        _state.update { it.copy(time = time) }
    }

    fun clear() {
        _state.update { DateTimeFieldState() }
    }
}
