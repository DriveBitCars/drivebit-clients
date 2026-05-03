package my.drivebit.utils

import kotlinx.browser.window

const val IDENTIFIER = "identifier"
const val NEW_LOGIN = "newLogin"
const val NEW_PASSWORD = "newPassword"

enum class OTPRESULT {
    VerifyOtp,
    ChangeEmail,
    ChangePhone,
    ChangePassword,
    ;

    companion object {
        fun fromString(value: String): OTPRESULT? = entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

const val OTP_RESULT_PARAM = "otpResult"

const val RETURN_CAR_ID = "returnCarId"

const val START_AT = "startAt"

const val END_AT = "endAt"

const val REDIRECT_PATH = "redirect"

const val AUTO_BOOK_AFTER_LOGIN = "autoBook"

fun getUrlParameter(name: String): String {
    val queryString = window.location.search
    return getUrlParameterFromQueryString(queryString, name)
}

fun getUrlParameterFromQueryString(
    queryString: String,
    name: String,
): String {
    val params =
        queryString
            .substring(1)
            .split("&")
    return params.find { it.startsWith("$name=") }?.substringAfter("$name=")?.let {
        js("decodeURIComponent")(it) as String
    } ?: ""
}

fun getUrlParameterFromPath(
    path: String,
    name: String,
): String {
    val queryString = path.substringAfter("?", missingDelimiterValue = "")
    return getUrlParameterFromQueryString(queryString, name)
}

fun String.encodeUrlParameter(): String = js("encodeURIComponent")(this) as String

fun removeUrlQueryParam(name: String) {
    val raw = window.location.search.removePrefix("?")
    if (raw.isBlank()) return
    val parts = raw.split("&")
    val kept =
        parts.filter { part ->
            val key = part.substringBefore("=", part)
            !key.equals(name, ignoreCase = true)
        }
    if (kept.size == parts.size) return
    val newSearch = if (kept.isEmpty()) "" else "?${kept.joinToString("&")}"
    val newUrl = window.location.pathname + newSearch + window.location.hash
    window.history.replaceState(null, "", newUrl)
}
