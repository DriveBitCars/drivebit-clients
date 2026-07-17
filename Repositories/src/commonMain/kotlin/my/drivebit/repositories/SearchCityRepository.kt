package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import my.drivebit.network.services.City

interface SearchCityRepository {
    val city: Flow<City>

    fun setCity(city: City)

    fun clear()
}

internal class SearchCityRepositoryImpl : SearchCityRepository {
    private val _city = MutableStateFlow<City?>(null)

    override val city: Flow<City> = _city.filterNotNull()

    override fun setCity(city: City) {
        _city.value = city
    }

    override fun clear() {
        _city.value = null
    }
}
