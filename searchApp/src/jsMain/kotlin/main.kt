import my.drivebit.clients.SearchApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        SearchApp()
    }
}
