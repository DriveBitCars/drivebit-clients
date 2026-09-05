package my.drivebit.mobile.documents

import androidx.compose.runtime.Composable

data class PickedDocumentImage(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String,
)

@Composable
expect fun rememberDocumentImagePicker(
    allowMultiple: Boolean = false,
    onPicked: (List<PickedDocumentImage>) -> Unit,
): () -> Unit
