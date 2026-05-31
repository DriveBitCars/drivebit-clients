import my.drivebit.clients.NearbyApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        NearbyApp()
    }
}
