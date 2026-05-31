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
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.NoPhotoPlaceholder
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.network.services.Document
import my.drivebit.network.services.DocumentUploadType
import my.drivebit.network.services.Documents
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.utils.readAsBytes
import my.drivebit.utils.resolveMinioImageUrlForBrowser
import my.drivebit.viewmodels.DocumentsState
import my.drivebit.viewmodels.DocumentsViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

private data class DocumentSlotConfig(
    val documentType: String,
    val title: String,
)

private val DOCUMENT_SLOTS =
    listOf(
        DocumentSlotConfig(DocumentUploadType.PassportMainPageRus, "Первая страница паспорта"),
        DocumentSlotConfig(DocumentUploadType.PassportSecondaryPageRus, "Вторая страница паспорта"),
        DocumentSlotConfig(DocumentUploadType.DriverLicense, "Водительское удостоверение (лицевая сторона)"),
        DocumentSlotConfig(DocumentUploadType.DriverLicenseBack, "Водительское удостоверение (обратная сторона)"),
    )

private fun findDocumentByType(
    documents: List<Document>,
    type: String,
): Document? =
    documents
        .filter { it.type == type }
        .maxByOrNull { it.uploadDate ?: "" }

private fun getStatusLabel(status: String?): String =
    when (status) {
        "Validated" -> "Одобрен"
        "Pending" -> "Ожидает"
        "UnderReview" -> "На проверке"
        "Rejected" -> "Отклонён"
        "Expired" -> "Просрочен"
        else -> "Не загружен"
    }

private fun getStatusColor(status: String?) =
    when (status) {
        "Validated" -> CSSColors.Green
        "Rejected" -> CSSColors.Red
        else -> CSSColors.Gray600
    }

private fun canReupload(document: Document?): Boolean {
    if (document == null) return true
    return document.status != "Validated"
}

@Composable
private fun renderDocumentSlots(
    documents: List<Document>,
    uploadingType: String?,
    viewModel: DocumentsViewModel,
) {
    Div({
        style {
            marginBottom(24.px)
            fontSize(18.px)
            fontWeight("600")
            color(CSSColors.Black)
        }
    }) {
        Text("Паспорт")
    }
    DOCUMENT_SLOTS.take(2).forEach { slot ->
        DocumentSlot(
            slotConfig = slot,
            document = findDocumentByType(documents, slot.documentType),
            isUploading = uploadingType == slot.documentType,
            viewModel = viewModel,
        )
    }
    Div({
        style {
            marginTop(32.px)
            marginBottom(24.px)
            fontSize(18.px)
            fontWeight("600")
            color(CSSColors.Black)
        }
    }) {
        Text("Водительское удостоверение")
    }
    DOCUMENT_SLOTS.drop(2).forEach { slot ->
        DocumentSlot(
            slotConfig = slot,
            document = findDocumentByType(documents, slot.documentType),
            isUploading = uploadingType == slot.documentType,
            viewModel = viewModel,
        )
    }
}

@Composable
fun DocumentsPage() {
    val viewModel: DocumentsViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Мои документы")
            }

            FormSection {
                when (val currentState = state) {
                    is DocumentsState.Idle -> {
                        Loader()
                    }

                    is DocumentsState.Loading -> {
                        Loader()
                    }

                    is DocumentsState.Uploading -> {
                        renderDocumentSlots(
                            documents = currentState.documents,
                            uploadingType = currentState.documentType,
                            viewModel = viewModel,
                        )
                    }

                    is DocumentsState.Error -> {
                        TextError(currentState.message)
                    }

                    is DocumentsState.Success -> {
                        renderDocumentSlots(
                            documents = currentState.documents,
                            uploadingType = null,
                            viewModel = viewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentSlot(
    slotConfig: DocumentSlotConfig,
    document: Document?,
    isUploading: Boolean,
    viewModel: DocumentsViewModel,
) {
    val doc = document
    val documentsService: Documents = koinInject()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    var documentPreviewUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingUrl by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(doc?.id, doc?.url) {
        if (doc != null) {
            documentPreviewUrl = null
            resolveMinioImageUrlForBrowser(doc.url)?.let {
                documentPreviewUrl = it
                return@LaunchedEffect
            }
            runCatching {
                documentsService.getDocumentUrl(doc.id)
            }.onSuccess { url ->
                documentPreviewUrl = url
            }.onFailure { /* ignore - will show placeholder */ }
        } else {
            documentPreviewUrl = null
        }
    }
    val fileInputId = remember { "doc-upload-${slotConfig.documentType}-${kotlin.random.Random.nextInt()}" }
    val status = doc?.status
    val showUpload = canReupload(document)

    DisposableEffect(fileInputId) {
        val inputElement = kotlinx.browser.document.getElementById(fileInputId) as? org.w3c.dom.HTMLInputElement
        val changeHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
            val input = event.target as? org.w3c.dom.HTMLInputElement
            val file = input?.files?.item(0) as? org.w3c.files.File
            if (file != null && file.type.startsWith("image/")) {
                coroutineScope.launch {
                    val bytes = file.readAsBytes()
                    viewModel.uploadDocument(
                        documentType = slotConfig.documentType,
                        fileBytes = bytes,
                        fileName = file.name,
                        contentType = file.type.ifBlank { "image/jpeg" },
                    )
                }
            }
        }
        inputElement?.addEventListener("change", changeHandler)
        onDispose {
            inputElement?.removeEventListener("change", changeHandler)
        }
    }

    Div({
        style {
            padding(16.px)
            marginBottom(12.px)
            backgroundColor(CSSColors.White)
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(8.px)
                marginBottom(12.px)
            }
        }) {
            Span({
                style {
                    fontWeight("600")
                    fontSize(16.px)
                    color(CSSColors.Black)
                }
            }) {
                Text(slotConfig.title)
            }
            Span({
                style {
                    fontSize(14.px)
                    color(getStatusColor(status))
                }
            }) {
                Text(getStatusLabel(status))
            }
        }

        if (doc != null) {
            Div({
                style {
                    marginBottom(12.px)
                    width(100.percent)
                    height(120.px)
                    borderRadius(8.px)
                    overflow("hidden")
                }
            }) {
                val previewUrl = documentPreviewUrl
                if (previewUrl != null) {
                    Img(
                        src = previewUrl,
                        attrs = {
                            style {
                                width(100.percent)
                                height(120.px)
                                property("object-fit", "cover")
                            }
                        },
                    )
                } else {
                    NoPhotoPlaceholder(height = 120.px)
                }
            }
            Div({
                style {
                    display(DisplayStyle.Flex)
                    gap(8.px)
                    marginBottom(8.px)
                }
            }) {
                Button({
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(if (isLoadingUrl) CSSColors.Gray600 else CSSColors.Blue)
                        color(CSSColors.White)
                        border(0.px)
                        borderRadius(4.px)
                        cursor(if (isLoadingUrl) "wait" else "pointer")
                        fontSize(14.px)
                    }
                    onClick {
                        if (isLoadingUrl) return@onClick
                        errorMessage = null
                        isLoadingUrl = true
                        coroutineScope.launch {
                            runCatching {
                                val signedUrl = documentsService.getDocumentUrl(doc.id)
                                window.open(signedUrl, "_blank", "noopener,noreferrer")
                            }.onFailure { e ->
                                errorMessage = e.message ?: "Не удалось открыть документ"
                            }
                            isLoadingUrl = false
                        }
                    }
                }) {
                    Text(if (isLoadingUrl) "Загрузка..." else "Открыть")
                }
            }
        }

        if (errorMessage != null) {
            Div({
                style {
                    marginBottom(8.px)
                    fontSize(14.px)
                    color(CSSColors.Red)
                }
            }) {
                Text(errorMessage ?: "")
            }
        }

        if (showUpload) {
            Input(
                type = InputType.File,
                attrs = {
                    id(fileInputId)
                    attr("accept", "image/*")
                    style {
                        display(DisplayStyle.None)
                    }
                },
            )
            Button({
                style {
                    padding(8.px, 16.px)
                    backgroundColor(if (isUploading) CSSColors.Gray600 else CSSColors.Blue)
                    color(CSSColors.White)
                    border(0.px)
                    borderRadius(4.px)
                    cursor(if (isUploading) "wait" else "pointer")
                    fontSize(14.px)
                }
                onClick {
                    if (isUploading) return@onClick
                    kotlinx.browser.document
                        .getElementById(fileInputId)
                        ?.let { it as org.w3c.dom.HTMLElement }
                        ?.click()
                }
            }) {
                Text(
                    when {
                        isUploading -> "Загрузка..."
                        doc != null -> "Перезагрузить"
                        else -> "Загрузить"
                    },
                )
            }
        }

        doc?.uploadDate?.let { uploadDate ->
            Div({
                style {
                    marginTop(8.px)
                    fontSize(12.px)
                    color(CSSColors.Gray600)
                }
            }) {
                Text("Загружено: ${formatDate(uploadDate)}")
            }
        }
    }
}

private fun formatDate(dateString: String): String =
    runCatching {
        mapIso8601ToDateString(dateString)
    }.getOrDefault(dateString)
