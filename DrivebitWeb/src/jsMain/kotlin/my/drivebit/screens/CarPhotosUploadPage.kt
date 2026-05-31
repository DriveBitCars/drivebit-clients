package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.network.services.Photo
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.isProcessableCarPhotoImage
import my.drivebit.utils.isWebpSource
import my.drivebit.utils.processedCarPhotoFileName
import my.drivebit.utils.reencodeToJpeg
import my.drivebit.utils.reencodeToWebp
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun CarPhotosUploadPage(onPhotosUploaded: () -> Unit = {}) {
    val photoService: Photo = koinInject()
    val buttonViewModel = createButtonViewModel()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var uploadedCount by remember { mutableStateOf(0) }
    var selectedFilesCount by remember { mutableStateOf(0) }

    val fileInputId = remember { "car-photos-input-${kotlin.random.Random.nextInt()}" }
    val carIdParam = getUrlParameter("carId")
    val carId = carIdParam.toIntOrNull()

    DisposableEffect(fileInputId) {
        val inputElement =
            document.getElementById(
                fileInputId,
            ) as? org.w3c.dom.HTMLInputElement
        val changeHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
            val input = event.target as? org.w3c.dom.HTMLInputElement
            val fileList = input?.files
            if (fileList != null && carId != null) {
                selectedFilesCount = fileList.length
                uploadedCount = 0
                error = null

                coroutineScope.launch {
                    isLoading = true
                    runCatching {
                        val fileBytesList = mutableListOf<ByteArray>()
                        val fileNamesList = mutableListOf<String>()
                        val contentTypesList = mutableListOf<String>()

                        for (i in 0 until fileList.length) {
                            val file = fileList.item(i) as? org.w3c.files.File
                            if (file != null && isProcessableCarPhotoImage(file.type, file.name)) {
                                val asWebp = isWebpSource(file.type, file.name)
                                val fileBytes =
                                    if (asWebp) {
                                        file.reencodeToWebp(
                                            maxWidth = 1920,
                                            maxHeight = 1920,
                                            quality = 0.9,
                                        )
                                    } else {
                                        file.reencodeToJpeg(
                                            maxWidth = 1920,
                                            maxHeight = 1920,
                                            quality = 0.9,
                                        )
                                    }
                                fileBytesList.add(fileBytes)
                                fileNamesList.add(
                                    processedCarPhotoFileName(
                                        file.name,
                                        if (asWebp) "webp" else "jpg",
                                    ),
                                )
                                contentTypesList.add(
                                    if (asWebp) "image/webp" else "image/jpeg",
                                )
                            }
                        }

                        if (fileBytesList.isNotEmpty()) {
                            photoService.uploadCarPhotos(
                                carId = carId.toString(),
                                fileBytesList = fileBytesList,
                                fileNames = fileNamesList,
                                contentTypes = contentTypesList,
                            )
                            uploadedCount = fileBytesList.size
                            onPhotosUploaded()
                        } else {
                            error = "Пожалуйста, выберите хотя бы одно изображение"
                        }
                    }.onFailure { e ->
                        error = e.message ?: "Не удалось загрузить фотографии"
                    }.also {
                        isLoading = false
                    }
                }
            }
        }
        inputElement?.addEventListener("change", changeHandler)
        onDispose {
            inputElement?.removeEventListener("change", changeHandler)
        }
    }

    buttonViewModel.setState(
        if (!isLoading && carId != null) {
            ButtonState.Enabled
        } else {
            ButtonState.Disabled
        },
    )

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Добавить фотографии")
            }

            FormSection {
                if (carId == null) {
                    TextError("Ошибка: ID автомобиля не найден")
                } else {
                    Column(gap = 16.px) {
                        Column(
                            modifier = {
                                alignItems(AlignItems.Center)
                                justifyContent(JustifyContent.Center)
                                padding(32.px)
                                borderRadius(8.px)
                                property("border", "2px dashed ${CSSColors.Gray300String}")
                                cursor("pointer")
                                property("transition", "border-color 0.2s ease, background-color 0.2s ease")
                            },
                            attrs = {
                                onClick {
                                    val inputElement =
                                        document.getElementById(
                                            fileInputId,
                                        ) as? org.w3c.dom.HTMLInputElement
                                    inputElement?.click()
                                }
                                onMouseEnter {
                                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                        "border-color",
                                        CSSColors.BlueString,
                                    )
                                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                        "background-color",
                                        "rgba(59, 130, 246, 0.05)",
                                    )
                                }
                                onMouseLeave {
                                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                        "border-color",
                                        CSSColors.Gray300String,
                                    )
                                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                        "background-color",
                                        "transparent",
                                    )
                                }
                            },
                        ) {
                            Span({
                                style {
                                    fontSize(18.px)
                                    fontWeight("600")
                                    color(CSSColors.Black)
                                    marginBottom(8.px)
                                }
                            }) {
                                Text(if (isLoading) "Загрузка..." else "Выберите фотографии")
                            }
                            Span({
                                style {
                                    fontSize(14.px)
                                    color(CSSColors.Gray600)
                                }
                            }) {
                                Text("Нажмите, чтобы выбрать файлы с диска")
                            }
                        }

                        Input(
                            type = InputType.File,
                            attrs = {
                                id(fileInputId)
                                attr("accept", "image/*")
                                attr("multiple", "multiple")
                                style {
                                    display(DisplayStyle.None)
                                }
                            },
                        )

                        if (selectedFilesCount > 0 && !isLoading) {
                            Span({
                                style {
                                    fontSize(14.px)
                                    color(CSSColors.Gray600)
                                    textAlign("center")
                                }
                            }) {
                                Text("Выбрано файлов: $selectedFilesCount")
                            }
                        }

                        if (uploadedCount > 0) {
                            Span({
                                style {
                                    fontSize(14.px)
                                    color(CSSColors.Blue)
                                    textAlign("center")
                                }
                            }) {
                                Text("Загружено фотографий: $uploadedCount")
                            }
                        }

                        if (error != null) {
                            TextError(error ?: "Произошла ошибка")
                        }

                        Row(
                            justifyContent = JustifyContent.Center,
                            modifier = { marginTop(16.px) },
                        ) {
                            Div({
                                style {
                                    maxWidth(200.px)
                                    width(100.percent)
                                }
                            }) {
                                ActionButton(
                                    viewModel = buttonViewModel,
                                    enabledColor = CSSColors.Blue,
                                    text = if (isLoading) "Загрузка..." else "Пропустить",
                                    onClick = {
                                        onPhotosUploaded()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
