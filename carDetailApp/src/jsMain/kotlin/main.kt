import my.drivebit.clients.CarDetailApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        CarDetailApp()
    }
}
