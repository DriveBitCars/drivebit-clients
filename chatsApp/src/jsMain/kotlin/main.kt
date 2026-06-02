import my.drivebit.clients.ChatsApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        ChatsApp()
    }
}
