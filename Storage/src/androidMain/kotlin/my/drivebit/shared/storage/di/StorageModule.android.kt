package my.drivebit.shared.storage.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import my.drivebit.shared.storage.Storage
import my.drivebit.shared.storage.StorageImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val storageModule: Module =
    module {
        single<Settings> {
            val context = get<Context>()
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            SharedPreferencesSettings(sharedPrefs)
        }
        single<Storage> {
            StorageImpl(get())
        }
    }
