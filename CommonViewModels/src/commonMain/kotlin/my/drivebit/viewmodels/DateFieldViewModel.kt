package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DateFieldState(
    val date: String? = null,
    val isCalendarOpen: Boolean = false,
)

class DateFieldViewModel(
    initialDate: String? = null,
) {
    private val _state =
        MutableStateFlow(
            DateFieldState(
                date = initialDate,
                isCalendarOpen = false,
            ),
        )

    val state: StateFlow<DateFieldState>
        get() = _state.asStateFlow()

    fun openCalendar() {
        _state.update { it.copy(isCalendarOpen = true) }
    }

    fun closeCalendar() {
        _state.update { it.copy(isCalendarOpen = false) }
    }

    fun setDate(date: String?) {
        _state.update { it.copy(date = date) }
    }

    fun clearDate() {
        _state.update { it.copy(date = null) }
    }
}
