package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.shared.storage.Storage

object MyCityStorageKeys {
    const val ID_KEY = "my_city_id"
    const val NAME_KEY = "my_city_name"
}

interface MyCityRepository {
    suspend fun searchCities(query: String): List<City>

    val getSelectedCity: Flow<City>

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
        private const val MOSCOW_NAME = "Москва"
    }

    private val _selectedCityFlow = MutableStateFlow<City?>(null)
    internal val selectedCityFlow get() = _selectedCityFlow

    override suspend fun searchCities(query: String): List<City> = dictionary.searchCities(query)

    private suspend fun findMoscow(): City? {
        val moscowResults = searchCities(MOSCOW_NAME)
        return moscowResults.firstOrNull { it.name == MOSCOW_NAME }
    }

    private suspend fun loadSelectedCity(): City {
        val selectedCityIdString = storage.getString(MyCityStorageKeys.ID_KEY)
        val selectedCityName = storage.getString(MyCityStorageKeys.NAME_KEY)

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
            val city =
                City(
                    id = selectedCityId,
                    name = selectedCityName,
                )
            selectedCityFlow.value = city
            return city
        }

        if (selectedCityId != null) {
            val moscowResults = searchCities(MOSCOW_NAME)
            val selectedCity = moscowResults.firstOrNull { it.id == selectedCityId }
            if (selectedCity != null) {
                selectedCityFlow.value = selectedCity
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

    override val getSelectedCity: Flow<City> =
        flow {
            if (_selectedCityFlow.value == null) {
                loadSelectedCity()
            }
            emitAll(_selectedCityFlow.filterNotNull())
        }

    override fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        storage.putString(MyCityStorageKeys.ID_KEY, cityId.toString())
        storage.putString(MyCityStorageKeys.NAME_KEY, cityName)
        selectedCityFlow.value = City(id = cityId, name = cityName)
    }
}
