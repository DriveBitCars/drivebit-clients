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
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.isProcessableCarPhotoImage
import my.drivebit.utils.isWebpSource
import my.drivebit.utils.processedCarPhotoFileName
import my.drivebit.utils.reencodeToJpeg
import my.drivebit.utils.reencodeToWebp
import my.drivebit.viewmodels.CarPhotosState
import my.drivebit.viewmodels.CarPhotosViewModel
import my.drivebit.viewmodels.PhotoMoveDirection
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun CarPhotosPage() {
    val viewModel: CarPhotosViewModel = koinInject()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    val carIdParam = getUrlParameter("carId")
    val state by viewModel.state.collectAsState()

    val fileInputId = remember { "car-photos-input-${kotlin.random.Random.nextInt()}" }

    LaunchedEffect(carIdParam) {
        if (carIdParam.isNotBlank()) {
            viewModel.loadPhotos(carIdParam)
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
                        Column(gap = 16.px) {
                            if (currentState.photos.isNotEmpty()) {
                                Div({
                                    style {
                                        display(DisplayStyle.Grid)
                                        gridTemplateColumns("repeat(auto-fill, minmax(150px, 1fr))")
                                        gap(12.px)
                                        marginBottom(12.px)
                                    }
                                }) {
                                    currentState.photos.forEachIndexed { index, photo ->
                                        var showMenu by remember { mutableStateOf(false) }
                                        val canMoveUp = index > 0 && !currentState.isReordering
                                        val canMoveDown =
                                            index < currentState.photos.lastIndex && !currentState.isReordering
                                        Div({
                                            style {
                                                position(Position.Relative)
                                            }
                                        }) {
                                            Img(
                                                src = photo.previewUrl(),
                                                attrs = {
                                                    style {
                                                        width(100.percent)
                                                        height(200.px)
                                                        property("object-fit", "cover")
                                                        borderRadius(8.px)
                                                    }
                                                },
                                            )
                                            if (index == 0) {
                                                Div({
                                                    style {
                                                        position(Position.Absolute)
                                                        top(8.px)
                                                        left(8.px)
                                                        backgroundColor(rgba(0, 0, 0, 0.65))
                                                        color(CSSColors.White)
                                                        fontSize(12.px)
                                                        padding(4.px, 8.px)
                                                        borderRadius(6.px)
                                                    }
                                                }) {
                                                    Text("Главное")
                                                }
                                            }
                                            Div({
                                                style {
                                                    position(Position.Absolute)
                                                    bottom(8.px)
                                                    left(8.px)
                                                    display(DisplayStyle.Flex)
                                                    flexDirection(FlexDirection.Column)
                                                    gap(6.px)
                                                }
                                            }) {
                                                Button({
                                                    if (!canMoveUp) {
                                                        attr("disabled", "true")
                                                    }
                                                    onClick {
                                                        if (canMoveUp) {
                                                            viewModel.movePhoto(
                                                                carIdParam,
                                                                photo.id,
                                                                PhotoMoveDirection.Up,
                                                            )
                                                        }
                                                    }
                                                    style {
                                                        width(32.px)
                                                        height(32.px)
                                                        borderRadius(50.percent)
                                                        backgroundColor(CSSColors.White)
                                                        border(1.px, LineStyle.Solid, rgb(128, 128, 128))
                                                        cursor(if (canMoveUp) "pointer" else "default")
                                                        opacity(if (canMoveUp) 1.0 else 0.4)
                                                        display(DisplayStyle.Flex)
                                                        property("align-items", "center")
                                                        property("justify-content", "center")
                                                        property("box-shadow", "0 2px 4px rgba(0,0,0,0.2)")
                                                    }
                                                }) {
                                                    Text("↑")
                                                }
                                                Button({
                                                    if (!canMoveDown) {
                                                        attr("disabled", "true")
                                                    }
                                                    onClick {
                                                        if (canMoveDown) {
                                                            viewModel.movePhoto(
                                                                carIdParam,
                                                                photo.id,
                                                                PhotoMoveDirection.Down,
                                                            )
                                                        }
                                                    }
                                                    style {
                                                        width(32.px)
                                                        height(32.px)
                                                        borderRadius(50.percent)
                                                        backgroundColor(CSSColors.White)
                                                        border(1.px, LineStyle.Solid, rgb(128, 128, 128))
                                                        cursor(if (canMoveDown) "pointer" else "default")
                                                        opacity(if (canMoveDown) 1.0 else 0.4)
                                                        display(DisplayStyle.Flex)
                                                        property("align-items", "center")
                                                        property("justify-content", "center")
                                                        property("box-shadow", "0 2px 4px rgba(0,0,0,0.2)")
                                                    }
                                                }) {
                                                    Text("↓")
                                                }
                                            }
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
                                            if (fileList != null && carId.isNotBlank() && fileList.length > 0) {
                                                coroutineScope.launch {
                                                    println("📸 [CarPhotosPage] Starting file processing...")
                                                    val fileBytesList = mutableListOf<ByteArray>()
                                                    val fileNamesList = mutableListOf<String>()
                                                    val contentTypesList = mutableListOf<String>()

                                                    for (i in 0 until fileList.length) {
                                                        val file = fileList.item(i) as? org.w3c.files.File
                                                        if (file != null &&
                                                            isProcessableCarPhotoImage(file.type, file.name)
                                                        ) {
                                                            val fileSize = file.size.toInt()
                                                            println(
                                                                "📸 [CarPhotosPage] Processing file: ${file.name}, type: ${file.type}, size: $fileSize bytes",
                                                            )

                                                            val maxFileSize = 2 * 1024 * 1024
                                                            val useResize = fileSize > maxFileSize
                                                            val maxDim = if (useResize) 1920 else null
                                                            val quality = if (useResize) 0.75 else 0.9
                                                            val asWebp = isWebpSource(file.type, file.name)
                                                            runCatching {
                                                                val processedBytes =
                                                                    if (asWebp) {
                                                                        file.reencodeToWebp(
                                                                            maxWidth = maxDim,
                                                                            maxHeight = maxDim,
                                                                            quality = quality,
                                                                        )
                                                                    } else {
                                                                        file.reencodeToJpeg(
                                                                            maxWidth = maxDim,
                                                                            maxHeight = maxDim,
                                                                            quality = quality,
                                                                        )
                                                                    }
                                                                val outName =
                                                                    processedCarPhotoFileName(
                                                                        file.name,
                                                                        if (asWebp) "webp" else "jpg",
                                                                    )

                                                                fileBytesList.add(processedBytes)
                                                                fileNamesList.add(outName)
                                                                contentTypesList.add(
                                                                    if (asWebp) "image/webp" else "image/jpeg",
                                                                )
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
