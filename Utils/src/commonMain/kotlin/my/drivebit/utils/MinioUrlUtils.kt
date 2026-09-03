package my.drivebit.utils

private val directMinioUrlPattern =
    Regex(
        pattern = """(?:https?://)?[^/?#]+:9000""",
        options = setOf(RegexOption.IGNORE_CASE),
    )

private const val PAGES_DEV_HOST = "dev.drivebit.my"
private const val PRODUCTION_PROXY_ORIGIN = "https://drivebit.ru"

fun minioProxyOrigin(
    currentOrigin: String,
    currentHost: String,
): String =
    if (currentHost.equals(PAGES_DEV_HOST, ignoreCase = true)) {
        PRODUCTION_PROXY_ORIGIN
    } else {
        currentOrigin.trimEnd('/')
    }

fun resolveMinioUrlForHost(
    rawUrl: String,
    currentHost: String,
): String {
    val path = extractPathFromApiUrl(rawUrl)
    return if (currentHost.equals(PAGES_DEV_HOST, ignoreCase = true) && path.startsWith("/")) {
        "$PRODUCTION_PROXY_ORIGIN$path"
    } else {
        path
    }
}

fun isDirectMinioUrl(url: String): Boolean = directMinioUrlPattern.containsMatchIn(url)

fun extractPathFromApiUrl(apiUrl: String): String {
    if (!isDirectMinioUrl(apiUrl)) {
        return apiUrl
    }
    val portIndex = apiUrl.indexOf(":9000")
    if (portIndex < 0) {
        return apiUrl
    }
    val pathStart = apiUrl.indexOf('/', portIndex + 4)
    return if (pathStart >= 0) apiUrl.substring(pathStart) else apiUrl
}

/** Same-origin path for MinIO URLs (keeps presigned query string). Use in image src behind /privatebct/ nginx or dev proxy. */
fun resolveMinioImageUrlForBrowser(rawUrl: String?): String? {
    val trimmed = rawUrl?.trim().orEmpty()
    if (trimmed.isEmpty()) {
        return null
    }
    return if (isDirectMinioUrl(trimmed)) extractPathFromApiUrl(trimmed) else trimmed
}
