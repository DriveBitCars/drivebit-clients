package my.drivebit.mobile.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import my.drivebit.mobile.documents.rememberDocumentImagePicker
import my.drivebit.network.services.BookingInspectionActDto
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
import my.drivebit.ui.components.ApplicationTopBar
import my.drivebit.viewmodels.InspectionActAction
import my.drivebit.viewmodels.InspectionActEffect
import my.drivebit.viewmodels.InspectionActUiState
import my.drivebit.viewmodels.InspectionActViewModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

data class InspectionActScreen(
    val bookingId: String,
    val type: InspectionActType,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
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
                        val url = effect.download.downloadUrl?.trim().orEmpty()
                        if (url.isNotEmpty()) {
                            uriHandler.openUri(url)
                        }
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                ApplicationTopBar(
                    title = inspectionActTitle(type),
                    onBackClick = { navigator.pop() },
                )
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (val current = state) {
                    InspectionActUiState.Idle,
                    InspectionActUiState.Loading,
                    -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is InspectionActUiState.Error -> {
                        Text(
                            text = current.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        current.previousAct?.let { act ->
                            InspectionActReadyBody(
                                act = act,
                                viewerRole = current.viewerRole,
                                ownerId = current.ownerId,
                                renterId = current.renterId,
                                fuelInput = act.fuelRemaining?.toString().orEmpty(),
                                mileageInput = act.mileage?.toString().orEmpty(),
                                commentInput =
                                    current.viewerRole?.let { role ->
                                        when (role) {
                                            InspectionActViewerRole.Owner -> act.ownerComment.orEmpty()
                                            InspectionActViewerRole.Renter -> act.renterComment.orEmpty()
                                        }
                                            }.orEmpty(),
                                error = error,
                                actionsInProgress = actionsInProgress,
                                viewModel = viewModel,
                            )
                        }
                    }
                    is InspectionActUiState.Ready ->
                        InspectionActReadyBody(
                            act = current.act,
                            viewerRole = current.viewerRole,
                            ownerId = current.ownerId,
                            renterId = current.renterId,
                            fuelInput = current.fuelInput,
                            mileageInput = current.mileageInput,
                            commentInput = current.commentInput,
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
private fun InspectionActReadyBody(
    act: BookingInspectionActDto,
    viewerRole: InspectionActViewerRole?,
    ownerId: String,
    renterId: String,
    fuelInput: String,
    mileageInput: String,
    commentInput: String,
    error: String?,
    actionsInProgress: Set<InspectionActAction>,
    viewModel: InspectionActViewModel,
) {
    val canEditMetrics = viewerRole != null && act.canCurrentUserEditMetrics(viewerRole)
    val canEditComment = viewerRole != null && act.canCurrentUserEditComment(viewerRole)
    val canUploadPhotos = viewerRole != null && act.canCurrentUserUploadPhotos(viewerRole)
    val canSign = viewerRole != null && act.canCurrentUserSign(viewerRole)
    val pickPhotos =
        rememberDocumentImagePicker { bytes, fileName, contentType ->
            viewModel.uploadPhoto(
                bytes = bytes,
                fileName = fileName,
                contentType = contentType,
            )
        }

    error?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    Text(
        text = "№${act.actNumber} · ${inspectionActStatusLabel(act.status)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Text(
        text = "Топливо и пробег",
        style = MaterialTheme.typography.titleSmall,
    )
    OutlinedTextField(
        value = fuelInput,
        onValueChange = viewModel::setFuelInput,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(INSPECTION_ACT_FUEL_LABEL) },
        placeholder = { Text("100") },
        enabled = canEditMetrics,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
    OutlinedTextField(
        value = mileageInput,
        onValueChange = viewModel::setMileageInput,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(INSPECTION_ACT_MILEAGE_LABEL) },
        placeholder = { Text("120500") },
        enabled = canEditMetrics,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )

    Text(
        text = "Комментарий",
        style = MaterialTheme.typography.titleSmall,
    )
    OutlinedTextField(
        value = commentInput,
        onValueChange = viewModel::setCommentInput,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Комментарий") },
        enabled = canEditComment,
        minLines = 3,
        maxLines = 6,
    )
    act.ownerComment?.takeIf { it.isNotBlank() }?.let { comment ->
        Text(
            text = "Владелец: $comment",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    act.renterComment?.takeIf { it.isNotBlank() }?.let { comment ->
        Text(
            text = "Арендатор: $comment",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Text(
        text = "Фотографии автомобиля",
        style = MaterialTheme.typography.titleSmall,
    )
    if (canUploadPhotos) {
        OutlinedButton(
            onClick = pickPhotos,
            enabled = InspectionActAction.UploadPhoto !in actionsInProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (InspectionActAction.UploadPhoto in actionsInProgress) {
                    "Загрузка…"
                } else {
                    "Добавить фото"
                },
            )
        }
    }
    val photos = act.photos.orEmpty()
    if (photos.isEmpty()) {
        Text(
            text = "Фотографий пока нет",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        photos.forEach { photo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                photo.url?.takeIf { it.isNotBlank() }?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Фото автомобиля",
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (
                    viewerRole != null &&
                    photo.canCurrentUserDelete(
                        role = viewerRole,
                        ownerId = ownerId,
                        renterId = renterId,
                        canEditOwnerFields = act.canEditOwnerFields,
                        canEditRenterFields = act.canEditRenterFields,
                    )
                ) {
                    TextButton(
                        onClick = { viewModel.deletePhoto(photo.id) },
                        enabled = InspectionActAction.DeletePhoto !in actionsInProgress,
                    ) {
                        Text("Удалить")
                    }
                }
            }
        }
    }

    Text(
        text = "Подпись",
        style = MaterialTheme.typography.titleSmall,
    )
    if (act.isFullySigned) {
        Text(
            text = inspectionActStatusLabel(act.status),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        viewerRole?.let { currentRole ->
            currentUserSignStatusMessage(currentRole)?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            counterpartySignStatusMessage(currentRole)?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (canSign) {
        Button(
            onClick = viewModel::sign,
            enabled = InspectionActAction.Sign !in actionsInProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Подписать")
        }
    }
    if (act.hasPdf) {
        Button(
            onClick = viewModel::downloadPdf,
            enabled = InspectionActAction.DownloadPdf !in actionsInProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Скачать PDF")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
