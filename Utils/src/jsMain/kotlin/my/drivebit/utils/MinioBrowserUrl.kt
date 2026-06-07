package my.drivebit.utils

import kotlinx.browser.window

fun minioProxiedAbsoluteUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) {
        return trimmed
    }

    val pathWithQuery =
        when {
            isDirectMinioUrl(trimmed) -> extractPathFromApiUrl(trimmed)
            trimmed.startsWith("/") -> trimmed
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                if (isDirectMinioUrl(trimmed)) {
                    extractPathFromApiUrl(trimmed)
                } else {
                    return trimmed.replaceFirst("http://", "https://")
                }
            }
            else -> return trimmed
        }

    return "${window.location.origin.trimEnd('/')}$pathWithQuery"
}
