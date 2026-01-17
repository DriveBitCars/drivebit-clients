package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.readAsBytes
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.PassportUploadState
import my.drivebit.viewmodels.PassportUploadViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun PassportUploadPage(onPassportUploaded: () -> Unit = {}) {
    val viewModel: PassportUploadViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val buttonViewModel = createButtonViewModel()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    var selectedFile: org.w3c.files.File? by remember { mutableStateOf(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val autoCreate = getUrlParameter("autoCreate").takeIf { it.isNotBlank() } ?: "1"

    val fileInputId = remember { "passport-upload-input-${kotlin.random.Random.nextInt()}" }

    val isUploading = state is PassportUploadState.Uploading || state is PassportUploadState.CreatingCar
    val canUpload = selectedFile != null && !isUploading

    buttonViewModel.setState(
        if (canUpload) ButtonState.Enabled else ButtonState.Disabled,
    )

    DisposableEffect(fileInputId) {
        val inputElement =
            document.getElementById(fileInputId) as? org.w3c.dom.HTMLInputElement
        val changeHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
            val input = event.target as? org.w3c.dom.HTMLInputElement
            val file = input?.files?.item(0) as? org.w3c.files.File
            selectedFile = file
            selectedFileName = file?.name.orEmpty()
        }
        inputElement?.addEventListener("change", changeHandler)
        onDispose {
            inputElement?.removeEventListener("change", changeHandler)
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is PassportUploadState.Success -> {
                if (autoCreate == "1") {
                    viewModel.createCar()
                } else {
                    viewModel.reset()
                    onPassportUploaded()
                }
            }
            is PassportUploadState.CarCreated -> {
                viewModel.reset()
                onPassportUploaded()
            }
            else -> Unit
        }
    }

    val errorText = (state as? PassportUploadState.Error)?.message

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Загрузите паспорт")
            }

            FormSection {
                Input(
                    type = InputType.File,
                    attrs = {
                        id(fileInputId)
                        attr("accept", "image/*")
                    },
                )

                if (selectedFileName.isNotBlank()) {
                    Text("Файл: $selectedFileName")
                }

                if (!errorText.isNullOrBlank()) {
                    TextError(errorText ?: "Произошла ошибка")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        marginTop(16.px)
                    }
                }) {
                    Div({
                        style {
                            maxWidth(240.px)
                            width(100.percent)
                        }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text =
                                when {
                                    state is PassportUploadState.CreatingCar -> "Создание..."
                                    isUploading -> "Загрузка..."
                                    else -> "Загрузить"
                                },
                            onClick = {
                                val file = selectedFile ?: return@ActionButton
                                coroutineScope.launch {
                                    val bytes = file.readAsBytes()
                                    viewModel.upload(
                                        fileBytes = bytes,
                                        fileName = file.name,
                                        contentType = file.type.ifBlank { "application/octet-stream" },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
