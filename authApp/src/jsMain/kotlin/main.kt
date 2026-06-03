import my.drivebit.clients.AuthApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        AuthApp()
    }
}
