package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarPhotosState
import my.drivebit.viewmodels.CarPhotosViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun org.w3c.files.File.readAsBytes(): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@readAsBytes
        val reader = org.w3c.files.FileReader()
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

private suspend fun org.w3c.files.File.compressImage(
    maxWidth: Int = 1920,
    maxHeight: Int = 1920,
    quality: Double = 0.8,
): ByteArray =
    suspendCancellableCoroutine { continuation ->
        val file = this@compressImage
        val reader = org.w3c.files.FileReader()

        reader.onload = {
            val img = kotlinx.browser.document.createElement("img") as org.w3c.dom.HTMLImageElement
            img.addEventListener("load", { _ ->
                val canvas = kotlinx.browser.document.createElement("canvas") as org.w3c.dom.HTMLCanvasElement
                val ctx = canvas.getContext("2d") as? org.w3c.dom.CanvasRenderingContext2D

                if (ctx != null) {
                    val originalWidth = img.naturalWidth
                    val originalHeight = img.naturalHeight

                    val newWidth: Int
                    val newHeight: Int

                    if (originalWidth > maxWidth || originalHeight > maxHeight) {
                        val ratio = minOf(maxWidth.toDouble() / originalWidth, maxHeight.toDouble() / originalHeight)
                        newWidth = (originalWidth * ratio).toInt()
                        newHeight = (originalHeight * ratio).toInt()
                    } else {
                        newWidth = originalWidth
                        newHeight = originalHeight
                    }

                    canvas.width = newWidth
                    canvas.height = newHeight

                    ctx.drawImage(img, 0.0, 0.0, newWidth.toDouble(), newHeight.toDouble())

                    canvas.toBlob(
                        { blob: org.w3c.files.Blob? ->
                            if (blob != null) {
                                val reader2 = org.w3c.files.FileReader()
                                reader2.onload = {
                                    val arrayBuffer = reader2.result
                                    if (arrayBuffer != null) {
                                        val uint8Array: dynamic = js("new Uint8Array(arrayBuffer)")
                                        val length = uint8Array.length as Int
                                        val bytes = ByteArray(length)
                                        for (i in 0 until length) {
                                            bytes[i] = (uint8Array[i] as Number).toInt().toByte()
                                        }
                                        continuation.resume(bytes)
                                    } else {
                                        continuation.resumeWithException(Exception("Failed to read compressed image"))
                                    }
                                }
                                reader2.onerror = {
                                    continuation.resumeWithException(Exception("Failed to read compressed image"))
                                }
                                reader2.readAsArrayBuffer(blob)
                            } else {
                                continuation.resumeWithException(Exception("Failed to compress image"))
                            }
                        },
                        "image/jpeg",
                        quality,
                    )
                } else {
                    continuation.resumeWithException(Exception("Failed to get canvas context"))
                }
            })
            img.addEventListener("error", { _ ->
                continuation.resumeWithException(Exception("Failed to load image"))
            })
            img.src = reader.result as String
        }
        reader.onerror = {
            continuation.resumeWithException(Exception("Failed to read file"))
        }
        reader.readAsDataURL(file)
    }

@Composable
fun CarPhotosPage() {
    val viewModel: CarPhotosViewModel = koinInject()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    val carIdParam = remember { getUrlParameter("carId") }
    val state by viewModel.state.collectAsState()

    val fileInputId = remember { "car-photos-input-${kotlin.random.Random.nextInt()}" }

    LaunchedEffect(carIdParam) {
        carIdParam?.let { carId ->
            viewModel.loadPhotos(carId)
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Фотографии автомобиля")
            }

            FormSection {
                when (val currentState = state) {
                    is CarPhotosState.Loading -> {
                        Loader()
                    }

                    is CarPhotosState.Error -> {
                        TextError(currentState.message)
                    }

                    is CarPhotosState.Success -> {
                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(16.px)
                            }
                        }) {
                            if (currentState.photos.isNotEmpty()) {
                                Div({
                                    style {
                                        display(DisplayStyle.Grid)
                                        gridTemplateColumns("repeat(auto-fill, minmax(150px, 1fr))")
                                        gap(12.px)
                                        marginBottom(12.px)
                                    }
                                }) {
                                    currentState.photos.forEach { photo ->
                                        var showMenu by remember { mutableStateOf(false) }
                                        Div({
                                            style {
                                                position(Position.Relative)
                                            }
                                        }) {
                                            Img(
                                                src = photo.url,
                                                attrs = {
                                                    style {
                                                        width(100.percent)
                                                        height(200.px)
                                                        property("object-fit", "cover")
                                                        borderRadius(8.px)
                                                    }
                                                },
                                            )
                                            Button({
                                                onClick { showMenu = !showMenu }
                                                style {
                                                    position(Position.Absolute)
                                                    top(8.px)
                                                    right(8.px)
                                                    width(32.px)
                                                    height(32.px)
                                                    borderRadius(50.percent)
                                                    backgroundColor(CSSColors.White)
                                                    border(1.px, LineStyle.Solid, rgb(128, 128, 128))
                                                    cursor("pointer")
                                                    display(DisplayStyle.Flex)
                                                    property("align-items", "center")
                                                    property("justify-content", "center")
                                                    property("box-shadow", "0 2px 4px rgba(0,0,0,0.2)")
                                                }
                                            }) {
                                                Text("⋮")
                                            }
                                            if (showMenu) {
                                                Div({
                                                    onClick { showMenu = false }
                                                    style {
                                                        position(Position.Fixed)
                                                        top(0.px)
                                                        left(0.px)
                                                        width(100.vw)
                                                        height(100.vh)
                                                        property("z-index", "1000")
                                                    }
                                                }) {}
                                                Div({
                                                    style {
                                                        position(Position.Absolute)
                                                        top(40.px)
                                                        right(8.px)
                                                        backgroundColor(CSSColors.White)
                                                        borderRadius(8.px)
                                                        property("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                                                        property("z-index", "1001")
                                                        minWidth(120.px)
                                                        padding(8.px)
                                                    }
                                                }) {
                                                    Button({
                                                        onClick {
                                                            viewModel.deletePhoto(carIdParam ?: "", photo.id)
                                                            showMenu = false
                                                        }
                                                        style {
                                                            width(100.percent)
                                                            padding(8.px, 12.px)
                                                            backgroundColor(rgba(0, 0, 0, 0))
                                                            border(0.px)
                                                            color(CSSColors.Red)
                                                            cursor("pointer")
                                                            textAlign("left")
                                                            fontSize(14.px)
                                                        }
                                                    }) {
                                                        Text("Удалить")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    flexDirection(FlexDirection.Column)
                                    gap(8.px)
                                }
                            }) {
                                Input(
                                    type = InputType.File,
                                    attrs = {
                                        id(fileInputId)
                                        attr("accept", "image/*")
                                        attr("multiple", "true")
                                        onChange { event ->
                                            val input = event.target as? org.w3c.dom.HTMLInputElement
                                            val fileList = input?.files
                                            val carId = carIdParam
                                            println(
                                                "📸 [CarPhotosPage] File input changed via onChange. Files: ${fileList?.length ?: 0}, carId: $carId",
                                            )
                                            if (fileList != null && carId != null && fileList.length > 0) {
                                                coroutineScope.launch {
                                                    println("📸 [CarPhotosPage] Starting file processing...")
                                                    val fileBytesList = mutableListOf<ByteArray>()
                                                    val fileNamesList = mutableListOf<String>()
                                                    val contentTypesList = mutableListOf<String>()

                                                    for (i in 0 until fileList.length) {
                                                        val file = fileList.item(i) as? org.w3c.files.File
                                                        if (file != null && file.type.startsWith("image/")) {
                                                            val fileSize = file.size.toInt()
                                                            println(
                                                                "📸 [CarPhotosPage] Processing file: ${file.name}, type: ${file.type}, size: $fileSize bytes",
                                                            )

                                                            val maxFileSize = 2 * 1024 * 1024
                                                            runCatching {
                                                                val processedBytes: ByteArray
                                                                val processedFileName: String

                                                                if (fileSize > maxFileSize) {
                                                                    println(
                                                                        "⚠️ [CarPhotosPage] File ${file.name} is too large ($fileSize bytes), compressing...",
                                                                    )
                                                                    processedBytes =
                                                                        file.compressImage(
                                                                            maxWidth = 1920,
                                                                            maxHeight = 1920,
                                                                            quality = 0.75,
                                                                        )
                                                                    val fileName = file.name as String
                                                                    val lastDotIndex = fileName.lastIndexOf(".")
                                                                    val fileNameWithoutExt =
                                                                        if (lastDotIndex >=
                                                                            0
                                                                        ) {
                                                                            fileName.substring(0, lastDotIndex)
                                                                        } else {
                                                                            fileName
                                                                        }
                                                                    processedFileName = "$fileNameWithoutExt.jpg"
                                                                    println(
                                                                        "📸 [CarPhotosPage] File compressed successfully: ${file.name}, original size: $fileSize, compressed size: ${processedBytes.size}",
                                                                    )
                                                                } else {
                                                                    processedBytes = file.readAsBytes()
                                                                    processedFileName = file.name
                                                                    println(
                                                                        "📸 [CarPhotosPage] File processed successfully: ${file.name}, size: ${processedBytes.size}",
                                                                    )
                                                                }

                                                                fileBytesList.add(processedBytes)
                                                                fileNamesList.add(processedFileName)
                                                                contentTypesList.add("image/jpeg")
                                                            }.onFailure { e ->
                                                                println(
                                                                    "❌ [CarPhotosPage] Error processing file ${file.name}: ${e.message}",
                                                                )
                                                            }
                                                        } else {
                                                            println(
                                                                "⚠️ [CarPhotosPage] Skipping file: ${file?.name}, type: ${file?.type}",
                                                            )
                                                        }
                                                    }

                                                    if (fileBytesList.isNotEmpty()) {
                                                        println(
                                                            "📸 [CarPhotosPage] Uploading ${fileBytesList.size} files...",
                                                        )
                                                        viewModel.uploadPhotos(
                                                            carId = carId,
                                                            fileBytesList = fileBytesList,
                                                            fileNames = fileNamesList,
                                                            contentTypes = contentTypesList,
                                                        )
                                                    } else {
                                                        println("⚠️ [CarPhotosPage] No valid image files to upload")
                                                    }
                                                }
                                            } else {
                                                println(
                                                    "⚠️ [CarPhotosPage] Invalid file list or carId. fileList: $fileList, carId: $carId",
                                                )
                                            }
                                        }
                                        style {
                                            display(DisplayStyle.None)
                                        }
                                    },
                                )

                                ActionButton(
                                    enabledColor = CSSColors.Blue,
                                    text = if (currentState.isUploading) "Загрузка..." else "Добавить фотографии",
                                    onClick = {
                                        document.getElementById(fileInputId)?.let { element ->
                                            (element as? org.w3c.dom.HTMLInputElement)?.click()
                                        }
                                    },
                                )

                                if (currentState.uploadError != null) {
                                    TextError(currentState.uploadError ?: "Ошибка загрузки")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
