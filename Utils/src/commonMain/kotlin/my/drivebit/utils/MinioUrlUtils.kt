package my.drivebit.utils

private val directMinioUrlPattern =
    Regex(
        pattern = """(?:https?://)?[^/?#]+:9000""",
        options = setOf(RegexOption.IGNORE_CASE),
    )

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
