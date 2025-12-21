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
    private val json: Json = Json { ignoreUnknownKeys = true },
) : CachedRepository<T> {
    override fun get(): Flow<T> =
        flow {
            val cachedJson = settings.getString(key, "")
            if (cachedJson.isNotEmpty()) {
                try {
                    val cached = json.decodeFromString(serializer, cachedJson)
                    println("📦 [CachedRepository] Returning cached data from storage (key: $key)")
                    emit(cached)
                    return@flow
                } catch (e: Exception) {
                    println("⚠️ [CachedRepository] Failed to decode cached data (key: $key): ${e.message}")
                }
            }

            println("🌐 [CachedRepository] Cache miss, fetching from network (key: $key)...")
            val data = service()
            val jsonString = json.encodeToString(serializer, data)
            settings.putString(key, jsonString)
            println("💾 [CachedRepository] Data saved to storage (key: $key)")
            emit(data)
        }

    override fun clearCache() {
        println("🔄 [CachedRepository] Cache cleared from storage (key: $key)")
        settings.remove(key)
    }
}
