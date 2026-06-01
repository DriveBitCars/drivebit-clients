import my.drivebit.clients.App
import my.drivebit.shell.mountWebHeaderComposeIfPresent
import org.jetbrains.compose.web.renderComposable

fun main() {
    mountWebHeaderComposeIfPresent()
    renderComposable(rootElementId = "root") {
        App()
    }
}
