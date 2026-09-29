package my.drivebit.navigation

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.utils.stripWebBasePath

class NavigationState {
    private val _currentPath =
        MutableStateFlow(
            stripWebBasePath(window.location.pathname, window.location.hostname),
        )
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _windowShowRestoreCycle = MutableStateFlow(0)
    val windowShowRestoreCycle: StateFlow<Int> = _windowShowRestoreCycle.asStateFlow()

    fun updatePath(path: String) {
        _currentPath.value =
            stripWebBasePath(path, window.location.hostname)
    }

    fun getCurrentPath(): String = _currentPath.value

    fun notifyWindowShowRestoreFromCache() {
        _windowShowRestoreCycle.update { it + 1 }
    }
}
