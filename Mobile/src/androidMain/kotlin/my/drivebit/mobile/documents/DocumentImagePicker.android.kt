package my.drivebit.mobile.documents

import android.net.Uri
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
    allowMultiple: Boolean,
    onPicked: (List<PickedDocumentImage>) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun readUri(uri: Uri): PickedDocumentImage? {
        val resolver = context.contentResolver
        val bytes =
            withContext(Dispatchers.IO) {
                resolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: return null
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val name = uri.lastPathSegment ?: "image.jpg"
        return PickedDocumentImage(bytes, name, mime)
    }

    val singleLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                val picked = readUri(uri) ?: return@launch
                onPicked(listOf(picked))
            }
        }

    val multiLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
        ) { uris ->
            if (uris.isEmpty()) return@rememberLauncherForActivityResult
            scope.launch {
                val picked =
                    uris.mapNotNull { uri ->
                        readUri(uri)
                    }
                if (picked.isNotEmpty()) {
                    onPicked(picked)
                }
            }
        }

    return {
        if (allowMultiple) {
            multiLauncher.launch("image/*")
        } else {
            singleLauncher.launch("image/*")
        }
    }
}
