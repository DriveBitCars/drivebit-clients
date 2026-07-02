package my.drivebit.viewmodels

import my.drivebit.network.NetworkException

object ErrorHandler {
    fun humanizeApiError(raw: String?): String? {
        val normalized = raw?.trim()?.trim('"')?.takeIf { it.isNotBlank() } ?: return null
        return when {
            normalized.equals("AddressNotFound", ignoreCase = true) ->
                "Адрес не найден. Укажите один город, улицу и дом — без лишних городов в строке."
            normalized.contains("валидированных документов", ignoreCase = true) ->
                "Подтвердить сделку нельзя: загрузите документы в профиле и дождитесь их проверки."
            else -> normalized
        }
    }

    fun extractErrorMessage(
        exception: Throwable,
        defaultNetworkError: String = "Ошибка сети",
        defaultGenericError: String = "Произошла ошибка",
    ): String {
        val humanized = humanizeApiError(exception.message)
        if (humanized != null) {
            return humanized
        }
        return when (exception) {
            is NetworkException -> defaultNetworkError
            else -> defaultGenericError
        }
    }
}
