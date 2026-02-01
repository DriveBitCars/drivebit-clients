package my.drivebit.utils

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("unused")
suspend fun org.w3c.files.File.readAsBytes(): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@readAsBytes
        val reader = FileReader()
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
                continuation.resumeWithException(Exception("Failed to read file: arrayBuffer is null"))
            }
        }
        reader.onerror = {
            continuation.resumeWithException(Exception("Failed to read file"))
        }
        reader.readAsArrayBuffer(file)
    }

suspend fun org.w3c.files.File.reencodeToJpeg(
    maxWidth: Int? = null,
    maxHeight: Int? = null,
    quality: Double = 0.9,
): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val sourceFile = this@reencodeToJpeg
        val reader = FileReader()

        reader.onload = {
            val img = document.createElement("img") as HTMLImageElement

            img.addEventListener("load", { _ ->
                val canvas = document.createElement("canvas") as HTMLCanvasElement
                val ctx = canvas.getContext("2d") as? CanvasRenderingContext2D

                if (ctx == null) {
                    continuation.resumeWithException(Exception("Failed to get canvas context"))
                    return@addEventListener
                }

                val targetWidth: Int
                val targetHeight: Int

                val limitWidth = maxWidth ?: img.naturalWidth
                val limitHeight = maxHeight ?: img.naturalHeight

                if (img.naturalWidth > limitWidth || img.naturalHeight > limitHeight) {
                    val ratio =
                        minOf(
                            limitWidth.toDouble() / img.naturalWidth,
                            limitHeight.toDouble() / img.naturalHeight,
                        )
                    targetWidth = (img.naturalWidth * ratio).toInt()
                    targetHeight = (img.naturalHeight * ratio).toInt()
                } else {
                    targetWidth = img.naturalWidth
                    targetHeight = img.naturalHeight
                }

                canvas.width = targetWidth
                canvas.height = targetHeight

                ctx.drawImage(img, 0.0, 0.0, targetWidth.toDouble(), targetHeight.toDouble())

                canvas.toBlob(
                    { blob: Blob? ->
                        if (blob != null) {
                            val resultReader = FileReader()
                            resultReader.onload = {
                                val arrayBuffer = resultReader.result
                                if (arrayBuffer != null) {
                                    val uint8Array: dynamic = js("new Uint8Array(arrayBuffer)")
                                    val length = uint8Array.length as Int
                                    val bytes = ByteArray(length)
                                    for (i in 0 until length) {
                                        bytes[i] = (uint8Array[i] as Number).toInt().toByte()
                                    }
                                    continuation.resume(bytes)
                                } else {
                                    continuation.resumeWithException(Exception("Failed to read re-encoded image"))
                                }
                            }
                            resultReader.onerror = {
                                continuation.resumeWithException(Exception("Failed to read re-encoded image"))
                            }
                            resultReader.readAsArrayBuffer(blob)
                        } else {
                            continuation.resumeWithException(Exception("Failed to re-encode image"))
                        }
                    },
                    "image/jpeg",
                    quality,
                )
            })

            img.addEventListener("error", { _ ->
                continuation.resumeWithException(Exception("Failed to load image"))
            })

            img.src = reader.result as String
        }

        reader.onerror = {
            continuation.resumeWithException(Exception("Failed to read file"))
        }

        reader.readAsDataURL(sourceFile)
    }
