package my.drivebit.mobile.documents

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun rememberDocumentImagePicker(
    allowMultiple: Boolean,
    onPicked: (List<PickedDocumentImage>) -> Unit,
): () -> Unit {
    val uriHandler = LocalUriHandler.current
    return { uriHandler.openUri("https://drivebit.ru/documents") }
}
