package my.drivebit.navigation

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationState {
    private val _currentPath = MutableStateFlow(window.location.pathname)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _windowShowRestoreCycle = MutableStateFlow(0)
    val windowShowRestoreCycle: StateFlow<Int> = _windowShowRestoreCycle.asStateFlow()

    fun updatePath(path: String) {
        _currentPath.value = path
    }

    fun getCurrentPath(): String = _currentPath.value

    fun notifyWindowShowRestoreFromCache() {
        _windowShowRestoreCycle.update { it + 1 }
    }
}
