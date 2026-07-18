package my.drivebit.search

import my.drivebit.web.koin.WebKoinHost
import my.drivebit.web.koin.webKoinModules
import org.jetbrains.compose.web.renderComposable
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(webKoinModules + searchAppModule)
        }
    }
    renderComposable(rootElementId = "root") {
        WebKoinHost {
            SearchApp()
        }
    }
}
