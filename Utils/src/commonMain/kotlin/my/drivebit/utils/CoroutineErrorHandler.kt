package my.drivebit.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun <T> CoroutineScope.safeLaunch(
    isLoading: () -> Boolean,
    crossinline setLoading: (Boolean) -> Unit,
    crossinline setError: (String?) -> Unit,
    crossinline block: suspend () -> T,
) {
    if (isLoading()) return

    launch {
        setLoading(true)
        setError(null)
        runCatching {
            block()
        }.onFailure { e ->
            setError(e.message ?: "Произошла ошибка")
        }.also {
            setLoading(false)
        }
    }
}

inline fun <T> CoroutineScope.safeLaunchWithErrorHandler(
    isLoading: () -> Boolean,
    crossinline setLoading: (Boolean) -> Unit,
    crossinline setError: (String?) -> Unit,
    crossinline errorHandler: (Throwable) -> String,
    crossinline block: suspend () -> T,
) {
    if (isLoading()) return

    launch {
        setLoading(true)
        setError(null)
        runCatching {
            block()
        }.onFailure { e ->
            setError(errorHandler(e))
        }.also {
            setLoading(false)
        }
    }
}

inline fun CoroutineScope.safeLaunchSimple(
    crossinline setLoading: (Boolean) -> Unit,
    crossinline setError: (String?) -> Unit,
    crossinline block: suspend () -> Unit,
) {
    launch {
        setLoading(true)
        setError(null)
        runCatching {
            block()
        }.onFailure { e ->
            setError(e.message ?: "Произошла ошибка")
        }.also {
            setLoading(false)
        }
    }
}
