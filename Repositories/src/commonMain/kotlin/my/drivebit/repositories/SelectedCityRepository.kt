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
        settings.putInt(CITY_ID_KEY, cityId)
        settings.putString(CITY_NAME_KEY, cityName)
    }

    override fun getCityId(): Int? {
        val cityId = settings.getInt(CITY_ID_KEY, -1)
        return if (cityId == -1) null else cityId
    }

    override fun getCityName(): String? = settings.getStringOrNullIfEmpty(CITY_NAME_KEY)

    override fun clearCity() {
        settings.remove(CITY_ID_KEY)
        settings.remove(CITY_NAME_KEY)
    }
}
