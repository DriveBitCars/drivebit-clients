package my.drivebit.repositories

import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.shared.storage.Storage

interface MyCityRepository {
    suspend fun searchCities(query: String): List<City>

    suspend fun getSelectedCity(): City

    fun selectCity(
        cityId: Int,
        cityName: String,
    )
}

internal class MyCityRepositoryImpl(
    private val dictionary: Dictionary,
    private val storage: Storage,
) : MyCityRepository {
    companion object {
        const val SELECTED_CITY_ID_KEY = "my_city_id"
        const val SELECTED_CITY_NAME_KEY = "my_city_name"
        private const val MOSCOW_NAME = "Москва"
    }

    override suspend fun searchCities(query: String): List<City> = dictionary.searchCities(query)

    private suspend fun findMoscow(): City? {
        val moscowResults = searchCities(MOSCOW_NAME)
        return moscowResults.firstOrNull { it.name == MOSCOW_NAME }
    }

    override suspend fun getSelectedCity(): City {
        val selectedCityIdString = storage.getString(SELECTED_CITY_ID_KEY)
        val selectedCityName = storage.getString(SELECTED_CITY_NAME_KEY)

        if (selectedCityIdString.isEmpty()) {
            val moscow = findMoscow()
            if (moscow != null) {
                selectCity(moscow.id, moscow.name)
                return moscow
            }
            throw IllegalStateException("Could not find Moscow city")
        }

        val selectedCityId = selectedCityIdString.toIntOrNull()
        if (selectedCityId != null && selectedCityName.isNotEmpty()) {
            return City(
                id = selectedCityId,
                name = selectedCityName,
            )
        }

        if (selectedCityId != null) {
            val moscowResults = searchCities(MOSCOW_NAME)
            val selectedCity = moscowResults.firstOrNull { it.id == selectedCityId }
            if (selectedCity != null) {
                return selectedCity
            }
        }

        val moscow = findMoscow()
        if (moscow != null) {
            selectCity(moscow.id, moscow.name)
            return moscow
        }

        throw IllegalStateException("Could not find selected city or Moscow")
    }

    override fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        storage.putString(SELECTED_CITY_ID_KEY, cityId.toString())
        storage.putString(SELECTED_CITY_NAME_KEY, cityName)
    }
}
