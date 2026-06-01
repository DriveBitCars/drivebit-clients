package my.drivebit.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MountWebShell() {
    LaunchedEffect(Unit) {
        mountWebHeaderComposeIfPresent()
    }
}
