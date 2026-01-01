package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal class SettingsJsonCache<T>(
    private val key: String,
    private val serializer: KSerializer<T>,
    private val settings: Settings,
    private val json: Json = repositoriesJson,
) {
    fun loadOrNull(): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        if (raw.isEmpty()) return null
        return runCatching { json.decodeFromString(serializer, raw) }.getOrNull()
    }

    fun save(value: T) {
        settings.putString(key, json.encodeToString(serializer, value))
    }

    fun clear() {
        settings.remove(key)
    }
}
