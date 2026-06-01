import my.drivebit.clients.AppHeaderMount
import my.drivebit.web.koin.ensureWebKoinStarted
import org.jetbrains.compose.web.renderComposable

fun main() {
    ensureWebKoinStarted()
    renderComposable(rootElementId = "drivebit-app-header") {
        AppHeaderMount()
    }
}
