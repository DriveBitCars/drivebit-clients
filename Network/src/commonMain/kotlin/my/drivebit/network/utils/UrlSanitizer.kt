package my.drivebit.network.utils

object UrlSanitizer {
    fun ensureHttpsUrl(url: String): String {
        val sanitizedUrl = url.replace(" ", "%20")
        
        if (sanitizedUrl.contains("155.212.170.94:9000")) {
            val path = sanitizedUrl.substringAfter(":9000")
            val normalizedPath = if (path.startsWith("/")) path else "/$path"
            return "https://drivebit.my$normalizedPath"
        }
        
        return sanitizedUrl
    }
}
