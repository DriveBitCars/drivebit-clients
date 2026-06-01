package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
fun ResponsiveContainer(
    breakpointWidth: Int = 768,
    content: @Composable (isMobile: Boolean) -> Unit,
) {
    var isMobile by remember { mutableStateOf(window.innerWidth <= breakpointWidth) }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            isMobile = window.innerWidth <= breakpointWidth
        }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    content(isMobile)
}
