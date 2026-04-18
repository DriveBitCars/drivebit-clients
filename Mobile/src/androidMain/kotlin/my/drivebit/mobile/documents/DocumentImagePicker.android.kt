package my.drivebit.mobile.documents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberDocumentImagePicker(
    onPicked: (ByteArray, String, String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val resolver = context.contentResolver
            scope.launch {
                val bytes =
                    withContext(Dispatchers.IO) {
                        resolver.openInputStream(uri)?.use { it.readBytes() }
                    } ?: return@launch
                val mime = resolver.getType(uri) ?: "image/jpeg"
                val name = uri.lastPathSegment ?: "image.jpg"
                onPicked(bytes, name, mime)
            }
        }
    return { launcher.launch("image/*") }
}
