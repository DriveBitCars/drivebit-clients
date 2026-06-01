package my.drivebit.shell

import kotlinx.browser.document
import my.drivebit.clients.AppHeaderRoot
import my.drivebit.web.koin.ensureHeaderKoinStarted
import org.jetbrains.compose.web.renderComposable
import org.koin.core.context.GlobalContext

private var headerComposeMounted = false

fun mountWebHeaderComposeIfPresent() {
    if (headerComposeMounted) return
    val mount = document.getElementById("drivebit-header-compose") ?: return
    headerComposeMounted = true
    if (GlobalContext.getOrNull() == null) {
        ensureHeaderKoinStarted()
    }
    renderComposable(rootElementId = mount.id) {
        AppHeaderRoot()
    }
}
