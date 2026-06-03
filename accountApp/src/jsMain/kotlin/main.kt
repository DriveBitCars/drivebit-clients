import my.drivebit.clients.AccountApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        AccountApp()
    }
}
