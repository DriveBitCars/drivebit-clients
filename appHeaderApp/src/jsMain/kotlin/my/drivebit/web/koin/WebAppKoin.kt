package my.drivebit.web.koin

import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import my.drivebit.web.login.loginWebModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

val webKoinModules: List<Module> =
    listOf(
        storageModule,
        repositoriesModule,
        webModule,
        loginWebModule,
        commonViewModelsModule,
    )

fun ensureWebKoinStarted() {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(webKoinModules)
        }
    }
}
