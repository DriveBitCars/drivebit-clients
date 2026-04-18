package my.drivebit.mobile.documents

import androidx.compose.runtime.Composable

@Composable
expect fun rememberDocumentImagePicker(
    onPicked: (ByteArray, String, String) -> Unit,
): () -> Unit
