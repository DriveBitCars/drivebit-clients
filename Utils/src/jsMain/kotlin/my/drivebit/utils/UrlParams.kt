package my.drivebit.utils

import kotlinx.browser.window

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
