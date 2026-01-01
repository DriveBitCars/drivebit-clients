package my.drivebit.viewmodels

import my.drivebit.network.NetworkException

object ErrorHandler {
    fun extractErrorMessage(
        exception: Throwable,
        defaultNetworkError: String = "Ошибка сети",
        defaultGenericError: String = "Произошла ошибка",
    ): String =
        when (exception) {
            is NetworkException -> exception.message?.takeIf { it.isNotBlank() } ?: defaultNetworkError
            else -> exception.message?.takeIf { it.isNotBlank() } ?: defaultGenericError
        }
}
