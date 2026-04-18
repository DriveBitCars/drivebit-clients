package my.drivebit.mobile.documents

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun rememberDocumentImagePicker(
    onPicked: (ByteArray, String, String) -> Unit,
): () -> Unit {
    val uriHandler = LocalUriHandler.current
    return { uriHandler.openUri("https://drivebit.ru/documents") }
}
