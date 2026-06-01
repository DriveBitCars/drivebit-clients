import my.drivebit.clients.MyCarsApp
import my.drivebit.shell.mountWebHeaderComposeIfPresent
import org.jetbrains.compose.web.renderComposable

fun main() {
    mountWebHeaderComposeIfPresent()
    renderComposable(rootElementId = "root") {
        MyCarsApp()
    }
}
