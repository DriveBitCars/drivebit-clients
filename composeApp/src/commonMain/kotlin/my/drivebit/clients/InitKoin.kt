package my.drivebit.clients

import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.di.commonViewModelsModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(
            storageModule,
            repositoriesModule,
            commonViewModelsModule,
            commonAppModule,
        )
    }
}

private val commonAppModule =
    module {
    }
