package my.drivebit.utils

/**
 * MinIO sometimes returns absolute URLs to the storage host (direct DNS name).
 * Those are rewritten to site-relative paths for loading via the public nginx proxy.
 */
private const val MINIO_DIRECT_HOST_PORT = "api.drivebit.ru:9000"

fun isDirectMinioUrl(url: String): Boolean = url.contains(MINIO_DIRECT_HOST_PORT)

fun extractPathFromApiUrl(apiUrl: String): String =
    if (apiUrl.contains(MINIO_DIRECT_HOST_PORT)) {
        apiUrl.split(MINIO_DIRECT_HOST_PORT).last()
    } else {
        apiUrl
    }
