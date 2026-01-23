package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedCityRepository {
    fun saveCity(
        cityId: Int,
        cityName: String,
    )

    fun getCityId(): Int?

    fun getCityName(): String?

    fun clearCity()
}

internal class SelectedCityRepositoryImpl(
    private val settings: Settings,
) : SelectedCityRepository {
    companion object {
        private const val CITY_ID_KEY = "selected_city_id"
        private const val CITY_NAME_KEY = "selected_city_name"
    }

    override fun saveCity(
        cityId: Int,
        cityName: String,
    ) {
        println("🏙️ [SelectedCityRepository] Сохранение города: cityId=$cityId, cityName=$cityName")
        settings.putInt(CITY_ID_KEY, cityId)
        settings.putString(CITY_NAME_KEY, cityName)
        println("🏙️ [SelectedCityRepository] Город успешно сохранен")
    }

    override fun getCityId(): Int? {
        val cityId = settings.getInt(CITY_ID_KEY, -1)
        val result = if (cityId == -1) null else cityId
        println("🏙️ [SelectedCityRepository] Получение cityId: raw=$cityId, result=$result")
        return result
    }

    override fun getCityName(): String? = settings.getStringOrNullIfEmpty(CITY_NAME_KEY)

    override fun clearCity() {
        settings.remove(CITY_ID_KEY)
        settings.remove(CITY_NAME_KEY)
    }
}
