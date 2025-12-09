package my.drivebit.viewmodels

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.files.File
import org.w3c.files.FileReader as W3CFileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual typealias FileReader = File

private const val MAX_FILE_SIZE = 2 * 1024 * 1024

actual suspend fun AvatarUploadViewModelImpl.uploadAvatarFileImpl(file: FileReader) {
    val fileBytes = file.readAsBytes()
    val fileName = file.name
    val contentType = file.type.ifBlank { "image/jpeg" }
    uploadAvatar(fileBytes, fileName, contentType)
}

@Suppress("UNCHECKED_CAST")
private suspend fun File.readAsBytes(): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@readAsBytes
        val fileSize: Number = js("file.size") as Number
        if (fileSize.toDouble() > MAX_FILE_SIZE) {
            continuation.resumeWithException(
                Exception("Файл слишком большой. Максимальный размер: ${MAX_FILE_SIZE / 1024 / 1024}MB"),
            )
            return@suspendCancellableCoroutine
        }
        val reader = W3CFileReader()
        reader.onload = {
            val arrayBuffer = reader.result
            if (arrayBuffer != null) {
                val uint8Array: dynamic = js("new Uint8Array(arrayBuffer)")
                val length = uint8Array.length as Int
                val bytes = ByteArray(length)
                for (i in 0 until length) {
                    bytes[i] = (uint8Array[i] as Number).toInt().toByte()
                }
                continuation.resume(bytes)
            } else {
                continuation.resumeWithException(Exception("Failed to read file"))
            }
        }
        reader.onerror = {
            continuation.resumeWithException(Exception("File read error"))
        }
        reader.readAsArrayBuffer(file)
    }

