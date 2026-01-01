package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
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
    }

    private val cache =
        SettingsJsonCache(
            key = CACHED_CARS_KEY,
            serializer = ListSerializer(CarListItemResponse.serializer()),
            settings = settings,
        )

    override suspend fun getMyCar(): List<CarListItemResponse> {
        cache.loadOrNull()?.let { return it }
        val cars = carService.getMyCars()
        cache.save(cars)
        return cars
    }

    override fun refresh() {
        cache.clear()
    }
}
