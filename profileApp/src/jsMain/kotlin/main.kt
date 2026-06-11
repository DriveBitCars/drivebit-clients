import my.drivebit.clients.ProfileApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        ProfileApp()
    }
}
