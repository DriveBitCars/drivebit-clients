package my.drivebit.shared.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

/**
 * JS реализация фабрики Storage
 */
fun create(): Storage {
    val settings: Settings = StorageSettings()
    return StorageImpl(settings)
}
