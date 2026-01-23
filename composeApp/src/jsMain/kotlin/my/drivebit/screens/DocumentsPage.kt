package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.viewmodels.DocumentsState
import my.drivebit.viewmodels.DocumentsViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

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

                    is DocumentsState.Error -> {
                        TextError(currentState.message)
                    }

                    is DocumentsState.Success -> {
                        if (currentState.documents.isEmpty()) {
                            Div({
                                style {
                                    textAlign("center")
                                    padding(32.px)
                                    color(CSSColors.Gray600)
                                }
                            }) {
                                Text("У вас пока нет загруженных документов")
                            }
                        } else {
                            currentState.documents.forEach { document ->
                                DocumentItem(document = document)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentItem(document: Document) {
    val documentsService: Documents = koinInject()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    var isLoadingUrl by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                marginBottom(8.px)
                fontWeight("600")
                fontSize(16.px)
                color(CSSColors.Black)
            }
        }) {
            Text(document.fileName ?: "Документ без названия")
        }

        if (document.type != null) {
            Div({
                style {
                    marginBottom(8.px)
                    fontSize(14.px)
                    color(CSSColors.Gray600)
                }
            }) {
                Text("Тип: ${document.type}")
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
                Text(errorMessage ?: "Ошибка")
            }
        }

        Div({
            style {
                display(DisplayStyle.InlineBlock)
                padding(8.px, 16.px)
                backgroundColor(if (isLoadingUrl) CSSColors.Gray600 else CSSColors.Blue)
                color(CSSColors.White)
                borderRadius(4.px)
                cursor(if (isLoadingUrl) "wait" else "pointer")
                property("transition", "opacity 0.2s ease")
            }
            onClick {
                if (isLoadingUrl) return@onClick
                errorMessage = null
                isLoadingUrl = true
                coroutineScope.launch {
                    runCatching {
                        val signedUrl = documentsService.getDocumentUrl(document.id)
                        window.open(signedUrl, "_blank", "noopener,noreferrer")
                    }.onFailure { e ->
                        errorMessage = e.message ?: "Не удалось открыть документ"
                    }.also {
                        isLoadingUrl = false
                    }
                }
            }
        }) {
            Text(if (isLoadingUrl) "Загрузка..." else "Открыть документ")
        }

        val uploadDate = document.uploadDate
        if (uploadDate != null) {
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
