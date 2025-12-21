package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import my.drivebit.network.services.CarListItemResponse

interface MyCarRepository {
    suspend fun getMyCar(): List<CarListItemResponse>

    fun refresh()
}

internal class MyCarRepositoryImpl(
    private val carService: my.drivebit.network.services.Car,
    private val settings: Settings,
) : MyCarRepository {
    companion object {
        private const val CACHED_CARS_KEY = "my_cars_cache"
        private val json = Json { ignoreUnknownKeys = true }
    }

    init {
        println("🏗️ [MyCarRepository] Instance created (hashCode: ${hashCode()})")
        val cachedCount = loadCachedCars()?.size ?: 0
        println("📦 [MyCarRepository] Loaded from storage: $cachedCount items")
    }

    private fun loadCachedCars(): List<CarListItemResponse>? {
        val jsonString = settings.getString(CACHED_CARS_KEY, "")
        return if (jsonString.isEmpty()) {
            null
        } else {
            try {
                json.decodeFromString<List<CarListItemResponse>>(jsonString)
            } catch (e: Exception) {
                println("⚠️ [MyCarRepository] Failed to decode cached cars: ${e.message}")
                null
            }
        }
    }

    private fun saveCachedCars(cars: List<CarListItemResponse>) {
        val jsonString = json.encodeToString(cars)
        settings.putString(CACHED_CARS_KEY, jsonString)
    }

    override suspend fun getMyCar(): List<CarListItemResponse> {
        println("🔍 [MyCarRepository] getMyCar() called")
        val cached = loadCachedCars()
        if (cached != null) {
            println("📦 [MyCarRepository] Returning cached cars from storage (${cached.size} items)")
            return cached
        }

        println("🌐 [MyCarRepository] Cache miss, fetching from network...")
        println("📡 [MyCarRepository] Calling carService.getMyCars()...")
        val cars = carService.getMyCars()
        println("📡 [MyCarRepository] carService.getMyCars() returned ${cars.size} cars")
        saveCachedCars(cars)
        println("✅ [MyCarRepository] Cars cached to storage (${cars.size} items)")
        println("🔍 [MyCarRepository] getMyCar() returning ${cars.size} cars")
        return cars
    }

    override fun refresh() {
        println("🔄 [MyCarRepository] Cache cleared from storage")
        settings.remove(CACHED_CARS_KEY)
    }
}
