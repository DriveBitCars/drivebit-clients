package my.drivebit.web.koin

import androidx.compose.runtime.Composable
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.analytics.initializeHawkErrorTracking
import my.drivebit.web.di.headerWebModule
import my.drivebit.web.di.webExtensionModule
import my.drivebit.web.login.loginWebModule
import org.koin.compose.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module

val headerKoinModules: List<Module> =
    listOf(
        storageModule,
        repositoriesModule,
        headerWebModule,
        loginWebModule,
        commonViewModelsModule,
    )

val webKoinModules: List<Module> =
    headerKoinModules + webExtensionModule

private var webExtensionModuleLoaded = false

private fun ensureWebExtensionModuleLoaded() {
    if (webExtensionModuleLoaded || GlobalContext.getOrNull() == null) return
    loadKoinModules(webExtensionModule)
    webExtensionModuleLoaded = true
}

fun ensureHeaderKoinStarted() {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(headerKoinModules)
        }
    }
}

@Composable
fun WebKoinHost(content: @Composable () -> Unit) {
    initializeHawkErrorTracking()
    if (GlobalContext.getOrNull() == null) {
        KoinApplication(application = {
            modules(webKoinModules)
        }) {
            content()
        }
    } else {
        ensureWebExtensionModuleLoaded()
        content()
    }
}
