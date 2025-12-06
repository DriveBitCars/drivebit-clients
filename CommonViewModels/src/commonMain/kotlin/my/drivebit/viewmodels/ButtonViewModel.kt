package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ButtonState {
    data object Enabled : ButtonState

    data object Disabled : ButtonState

    data object Loading : ButtonState
}

class ButtonViewModel {
    private val _state = MutableStateFlow<ButtonState>(ButtonState.Enabled)
    val state: StateFlow<ButtonState> = _state.asStateFlow()

    val isEnabled: Boolean
        get() = _state.value is ButtonState.Enabled

    val isDisabled: Boolean
        get() = _state.value is ButtonState.Disabled

    val isLoading: Boolean
        get() = _state.value is ButtonState.Loading

    fun setState(state: ButtonState) {
        _state.value = state
    }
}
