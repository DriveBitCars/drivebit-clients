package my.drivebit.shared.storage.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import my.drivebit.shared.storage.AuthNotifyingStorage
import my.drivebit.shared.storage.Storage
import my.drivebit.shared.storage.StorageImpl
import my.drivebit.shared.storage.installCrossTabAuthSyncIfNeeded
import org.koin.core.module.Module
import org.koin.dsl.module

actual val storageModule: Module =
    module {
        single<Settings> {
            StorageSettings()
        }
        single<Storage> {
            installCrossTabAuthSyncIfNeeded()
            AuthNotifyingStorage(StorageImpl(get()))
        }
    }
