package my.drivebit.utils

private val KNOWN_CAR_PHOTO_EXTENSIONS =
    setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")

fun isWebpSource(
    mimeType: String,
    fileName: String,
): Boolean =
    mimeType.equals("image/webp", ignoreCase = true) ||
        fileName.substringAfterLast('.', "").lowercase() == "webp"

fun isProcessableCarPhotoImage(
    mimeType: String,
    fileName: String,
): Boolean {
    if (mimeType.startsWith("image/")) return true
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in KNOWN_CAR_PHOTO_EXTENSIONS
}

fun processedCarPhotoFileName(
    originalName: String,
    outputExtension: String,
): String {
    val nameWithoutExt = originalName.substringBeforeLast(".", originalName)
    return if (nameWithoutExt.isNotBlank()) {
        "$nameWithoutExt.$outputExtension"
    } else {
        "photo.$outputExtension"
    }
}
