import my.drivebit.clients.App
import my.drivebit.clients.AppHeaderRoot
import my.drivebit.web.koin.ensureWebKoinStarted
import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable

fun main() {
    ensureWebKoinStarted()
    document.getElementById("drivebit-header-compose")?.let { mount ->
        renderComposable(rootElementId = mount.id) {
            AppHeaderRoot()
        }
    }
    renderComposable(rootElementId = "root") {
        App()
    }
}
