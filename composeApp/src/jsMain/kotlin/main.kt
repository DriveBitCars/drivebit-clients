import my.drivebit.clients.App
import my.drivebit.clients.AppHeaderRoot
import my.drivebit.web.koin.ensureWebKoinStarted
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable

fun main() {
    ensureWebKoinStarted()
    if (document.getElementById("drivebit-app-header") != null) {
        renderComposable(rootElementId = "drivebit-app-header") {
            AppHeaderRoot()
        }
    }
    renderComposable(rootElementId = "root") {
        App()
    }
}
