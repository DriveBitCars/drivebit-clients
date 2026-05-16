package my.drivebit.viewmodels

import my.drivebit.network.NetworkException

object ErrorHandler {
    fun humanizeApiError(raw: String?): String? =
        when (raw?.trim()?.trim('"')) {
            "AddressNotFound" ->
                "Адрес не найден. Укажите один город, улицу и дом — без лишних городов в строке."
            else -> raw?.takeIf { it.isNotBlank() }
        }

    fun extractErrorMessage(
        exception: Throwable,
        defaultNetworkError: String = "Ошибка сети",
        defaultGenericError: String = "Произошла ошибка",
    ): String =
        when (exception) {
            is NetworkException ->
                humanizeApiError(exception.message)
                    ?: defaultNetworkError
            else ->
                humanizeApiError(exception.message)
                    ?: defaultGenericError
        }
}
