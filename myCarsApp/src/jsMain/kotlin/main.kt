import my.drivebit.clients.MyCarsApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        MyCarsApp()
    }
}
