package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface CachedRepository<T> {
    fun get(): Flow<T>

    fun clearCache()
}

internal class CachedRepositoryImpl<T>(
    private val key: String,
    private val serializer: KSerializer<T>,
    private val service: suspend () -> T,
    private val settings: Settings,
    private val json: Json = repositoriesJson,
) : CachedRepository<T> {
    override fun get(): Flow<T> =
        flow {
            val cachedJson = settings.getString(key, "")
            if (cachedJson.isNotEmpty()) {
                try {
                    val cached = json.decodeFromString(serializer, cachedJson)
                    emit(cached)
                    return@flow
                } catch (e: Exception) {
                }
            }

            val data = service()
            val jsonString = json.encodeToString(serializer, data)
            settings.putString(key, jsonString)
            emit(data)
        }

    override fun clearCache() {
        settings.remove(key)
    }
}
