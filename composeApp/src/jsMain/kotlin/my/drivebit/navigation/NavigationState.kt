package my.drivebit.navigation

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationState {
    private val _currentPath = MutableStateFlow(window.location.pathname)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    fun updatePath(path: String) {
        _currentPath.value = path
    }

    fun getCurrentPath(): String = _currentPath.value
}
