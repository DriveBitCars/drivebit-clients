package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.INSPECTION_ACT_FUEL_LABEL
import my.drivebit.network.services.INSPECTION_ACT_MILEAGE_LABEL
import my.drivebit.network.services.InspectionActType
import my.drivebit.network.services.InspectionActViewerRole
import my.drivebit.network.services.canCurrentUserDelete
import my.drivebit.network.services.canCurrentUserEditComment
import my.drivebit.network.services.canCurrentUserEditMetrics
import my.drivebit.network.services.canCurrentUserSign
import my.drivebit.network.services.canCurrentUserUploadPhotos
import my.drivebit.network.services.counterpartySignStatusMessage
import my.drivebit.network.services.currentUserSignStatusMessage
import my.drivebit.network.services.inspectionActStatusLabel
import my.drivebit.network.services.inspectionActTitle
import my.drivebit.shared.storage.Storage
import my.drivebit.shell.PageWithLogo
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.minioProxiedAbsoluteUrl
import my.drivebit.utils.readAsBytes
import my.drivebit.viewmodels.InspectionActAction
import my.drivebit.viewmodels.InspectionActEffect
import my.drivebit.viewmodels.InspectionActUiState
import my.drivebit.viewmodels.InspectionActViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun InspectionActPage() {
    val storage: Storage = koinInject()
    val navigationController = LocalNavigationController.current
    val bookingId = getUrlParameter("bookingId").trim()
    val typeName = getUrlParameter("type").trim()
    val type = InspectionActType.entries.firstOrNull { it.name.equals(typeName, ignoreCase = true) }

    if (bookingId.isBlank() || type == null) {
        PageWithLogo {
            CenteredFormContainer(maxWidth = 640.px) {
                TextError("В ссылке не указаны bookingId и type (Handover|Return).")
                Button({
                    style { marginTop(16.px) }
                    onClick { navigationController?.navigateTo("/my-bookings") }
                }) {
                    Text("Назад")
                }
            }
        }
        return
    }

    if (!storage.isLogined()) {
        LaunchedEffect(bookingId, type) {
            val returnTo = window.location.pathname + window.location.search
            window.location.href = "/login-by-phone?$REDIRECT_PATH=${returnTo.encodeUrlParameter()}"
        }
        PageWithLogo {
            Loader()
        }
        return
    }

    val koinScope = currentKoinScope()
    val viewModel: InspectionActViewModel =
        remember(bookingId, type) {
            koinScope.get(parameters = { parametersOf(bookingId, type) })
        }
    val state by viewModel.state.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionsInProgress by viewModel.actionsInProgress.collectAsState()

    LaunchedEffect(bookingId, type) {
        viewModel.load()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is InspectionActEffect.OpenPdf -> {
                    val url = effect.download.downloadUrl?.let(::minioProxiedAbsoluteUrl)
                    if (!url.isNullOrBlank()) {
                        window.open(url, "_blank")
                    }
                }
            }
        }
    }

    PageWithLogo {
        CenteredFormContainer(maxWidth = 720.px) {
            PageHeader {
                TextSmartHeader(inspectionActTitle(type))
            }
            FormSection {
                when (val current = state) {
                    InspectionActUiState.Idle,
                    InspectionActUiState.Loading,
                    -> Loader()
                    is InspectionActUiState.Error -> {
                        TextError(current.message)
                        current.previousAct?.let { act ->
                            InspectionActReadyContent(
                                ready =
                                    InspectionActUiState.Ready(
                                        act = act,
                                        viewerRole = current.viewerRole,
                                        ownerId = current.ownerId,
                                        renterId = current.renterId,
                                        ownerName = current.ownerName,
                                        renterName = current.renterName,
                                        fuelInput = act.fuelRemaining?.toString().orEmpty(),
                                        mileageInput = act.mileage?.toString().orEmpty(),
                                        commentInput =
                                            current.viewerRole?.let { role ->
                                                when (role) {
                                                    InspectionActViewerRole.Owner -> act.ownerComment.orEmpty()
                                                    InspectionActViewerRole.Renter -> act.renterComment.orEmpty()
                                                }
                                            }.orEmpty(),
                                    ),
                                error = error,
                                actionsInProgress = actionsInProgress,
                                viewModel = viewModel,
                            )
                        }
                    }
                    is InspectionActUiState.Ready ->
                        InspectionActReadyContent(
                            ready = current,
                            error = error,
                            actionsInProgress = actionsInProgress,
                            viewModel = viewModel,
                        )
                }
            }
        }
    }
}

@Composable
private fun InspectionActReadyContent(
    ready: InspectionActUiState.Ready,
    error: String?,
    actionsInProgress: Set<InspectionActAction>,
    viewModel: InspectionActViewModel,
) {
    val act = ready.act
    val role = ready.viewerRole
    val canEditMetrics = role != null && act.canCurrentUserEditMetrics(role)
    val canEditComment = role != null && act.canCurrentUserEditComment(role)
    val canUploadPhotos = role != null && act.canCurrentUserUploadPhotos(role)
    val canSign = role != null && act.canCurrentUserSign(role)
    val fileInputId = remember { "inspection-act-photos-${kotlin.random.Random.nextInt()}" }
    val uploadScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    DisposableEffect(fileInputId) {
        val inputElement = document.getElementById(fileInputId) as? org.w3c.dom.HTMLInputElement
        val changeHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
            val input = event.target as? org.w3c.dom.HTMLInputElement
            val fileList = input?.files
            if (fileList != null && fileList.length > 0) {
                uploadScope.launch {
                    for (index in 0 until fileList.length) {
                        val file = fileList.item(index) as? org.w3c.files.File ?: continue
                        val bytes = file.readAsBytes()
                        viewModel.uploadPhoto(
                            bytes = bytes,
                            fileName = file.name,
                            contentType = file.type.ifBlank { "image/jpeg" },
                        )
                    }
                }
            }
        }
        inputElement?.addEventListener("change", changeHandler)
        onDispose {
            inputElement?.removeEventListener("change", changeHandler)
        }
    }

    Column(gap = 16.px, modifier = { width(100.percent) }) {
        if (error != null) {
            TextError(error)
        }
        Span({ style { fontSize(14.px); color(CSSColors.Gray600) } }) {
            Text("№${act.actNumber} · ${inspectionActStatusLabel(act.status)}")
        }
        Column(gap = 4.px) {
            Span({ style { fontSize(14.px) } }) {
                Text("Владелец: ${ready.ownerName.ifBlank { "Владелец" }}")
            }
            Span({ style { fontSize(14.px) } }) {
                Text("Арендатор: ${ready.renterName.ifBlank { "Арендатор" }}")
            }
        }

        Column(gap = 8.px) {
            Span({ style { fontWeight("600") } }) { Text("Топливо и пробег") }
            Row(gap = 12.px, modifier = { width(100.percent) }) {
                Column(gap = 4.px, modifier = { flex(1) }) {
                    Span({ style { fontSize(13.px); color(CSSColors.Gray600) } }) {
                        Text(INSPECTION_ACT_FUEL_LABEL)
                    }
                    Input(InputType.Text) {
                        value(ready.fuelInput)
                        attr("placeholder", "100")
                        attr("inputmode", "numeric")
                        if (!canEditMetrics) attr("disabled", "true")
                        onInput { viewModel.setFuelInput(it.value) }
                    }
                }
                Column(gap = 4.px, modifier = { flex(1) }) {
                    Span({ style { fontSize(13.px); color(CSSColors.Gray600) } }) {
                        Text(INSPECTION_ACT_MILEAGE_LABEL)
                    }
                    Input(InputType.Text) {
                        value(ready.mileageInput)
                        attr("placeholder", "120500")
                        attr("inputmode", "numeric")
                        if (!canEditMetrics) attr("disabled", "true")
                        onInput { viewModel.setMileageInput(it.value) }
                    }
                }
            }
        }

        Column(gap = 8.px) {
            Span({ style { fontWeight("600") } }) { Text("Комментарий") }
            TextArea {
                value(ready.commentInput)
                if (!canEditComment) attr("disabled", "true")
                onInput { event -> viewModel.setCommentInput(event.value) }
                style { width(100.percent); height(96.px) }
            }
            if (!act.ownerComment.isNullOrBlank()) {
                Span({ style { color(CSSColors.Gray600); fontSize(13.px) } }) {
                    Text("Комментарий владельца: ${act.ownerComment}")
                }
            }
            if (!act.renterComment.isNullOrBlank()) {
                Span({ style { color(CSSColors.Gray600); fontSize(13.px) } }) {
                    Text("Комментарий арендатора: ${act.renterComment}")
                }
            }
        }

        Column(gap = 8.px) {
            Span({ style { fontWeight("600") } }) { Text("Фотографии автомобиля") }
            if (canUploadPhotos) {
                Input(InputType.File) {
                    id(fileInputId)
                    attr("accept", "image/*")
                    attr("multiple", "true")
                }
            }
            val photos = act.photos.orEmpty()
            if (photos.isEmpty()) {
                Span({ style { color(CSSColors.Gray600) } }) { Text("Фотографий пока нет") }
            } else {
                photos.forEach { photo ->
                    Row(gap = 8.px, alignItems = AlignItems.Center) {
                        val photoUrl = photo.url?.takeIf { it.isNotBlank() }?.let(::minioProxiedAbsoluteUrl)
                        if (photoUrl != null) {
                            Img(src = photoUrl) {
                                style { width(72.px); height(72.px); property("object-fit", "cover") }
                            }
                        }
                        if (
                            role != null &&
                            photo.canCurrentUserDelete(
                                role = role,
                                ownerId = ready.ownerId,
                                renterId = ready.renterId,
                                canEditOwnerFields = act.canEditOwnerFields,
                                canEditRenterFields = act.canEditRenterFields,
                            )
                        ) {
                            Button({
                                onClick { viewModel.deletePhoto(photo.id) }
                                if (InspectionActAction.DeletePhoto in actionsInProgress) {
                                    disabled()
                                }
                            }) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }

        Column(gap = 8.px) {
            Span({ style { fontWeight("600") } }) { Text("Подпись") }
            if (act.isFullySigned) {
                Span({ style { color(CSSColors.Gray600); fontSize(14.px) } }) {
                    Text(inspectionActStatusLabel(act.status))
                }
            } else {
                role?.let { currentRole ->
                    act.currentUserSignStatusMessage(currentRole)?.let { message ->
                        Span({ style { fontSize(14.px); color(CSSColors.Gray600) } }) { Text(message) }
                    }
                    act.counterpartySignStatusMessage(currentRole)?.let { message ->
                        Span({ style { fontSize(13.px); color(CSSColors.Gray600) } }) { Text(message) }
                    }
                }
            }
            if (canSign) {
                Button({
                    onClick { viewModel.sign() }
                    if (InspectionActAction.Sign in actionsInProgress) {
                        disabled()
                    }
                }) {
                    Text("Подписать")
                }
            }
            if (act.hasPdf) {
                Button({
                    onClick { viewModel.downloadPdf() }
                    if (InspectionActAction.DownloadPdf in actionsInProgress) {
                        disabled()
                    }
                }) {
                    Text("Скачать PDF")
                }
            }
        }
    }
}
