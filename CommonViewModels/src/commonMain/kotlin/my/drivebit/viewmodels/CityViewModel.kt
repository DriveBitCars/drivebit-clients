package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.City
import my.drivebit.repositories.CityRepository
import my.drivebit.repositories.ResultCities

class CityViewModel(
    private val cityRepository: CityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isNotBlank()) {
            searchCities(newQuery)
        } else {
            _cities.value = emptyList()
        }
    }

    private fun searchCities(query: String) {
        coroutineScope.launch {
            _error.value = null
            when (val result = cityRepository.searchCities(query)) {
                is ResultCities.Success -> {
                    _cities.value = result.cities
                }
                is ResultCities.Error -> {
                    _error.value = result.message
                    _cities.value = emptyList()
                }
            }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _cities.value = emptyList()
        _error.value = null
    }
}
