package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import my.drivebit.network.services.City
import my.drivebit.repositories.CityRepository
import my.drivebit.repositories.ResultCities

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CityViewModel(
    private val cityRepository: CityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val debounceTimeMs: Long = 300,
) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _browseAllCitiesMode = MutableStateFlow(false)
    private val _fullCityCatalog = MutableStateFlow<List<City>>(emptyList())

    init {
        _query
            .debounce(debounceTimeMs)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    if (_browseAllCitiesMode.value && _fullCityCatalog.value.isNotEmpty()) {
                        applyBrowseAllFilter(query.trim())
                        emit(Unit)
                        return@flow
                    }
                    if (query.isNotBlank()) {
                        searchCities(query)
                    } else {
                        _cities.value = emptyList()
                        _error.value = null
                    }
                    emit(Unit)
                }
            }.launchIn(coroutineScope)
    }

    fun enableBrowseAllCitiesMode() {
        coroutineScope.launch {
            _browseAllCitiesMode.value = true
            _error.value = null
            when (val result = cityRepository.getAllCities()) {
                is ResultCities.Success -> {
                    _fullCityCatalog.value = result.cities
                    applyBrowseAllFilter(_query.value.trim())
                }
                is ResultCities.Error -> {
                    _fullCityCatalog.value = emptyList()
                    _cities.value = emptyList()
                    _error.value = result.message
                }
            }
        }
    }

    fun setSearchOnlyMode() {
        _browseAllCitiesMode.value = false
        _fullCityCatalog.value = emptyList()
        _query.value = ""
        _cities.value = emptyList()
        _error.value = null
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    private fun applyBrowseAllFilter(trimmedQuery: String) {
        _error.value = null
        val catalog = _fullCityCatalog.value
        val filtered =
            if (trimmedQuery.isEmpty()) {
                catalog
            } else {
                catalog.filter { city ->
                    city.name.contains(trimmedQuery, ignoreCase = true) ||
                        (city.regionName?.contains(trimmedQuery, ignoreCase = true) == true)
                }
            }
        _cities.value = filtered.sortedBy { it.name }
    }

    private suspend fun searchCities(query: String) {
        _error.value = null
        when (val result = cityRepository.searchCities(query)) {
            is ResultCities.Success -> {
                if (_query.value == query) {
                    _cities.value = result.cities
                }
            }
            is ResultCities.Error -> {
                if (_query.value == query) {
                    _error.value = result.message
                    _cities.value = emptyList()
                }
            }
        }
    }

    fun clearQuery() {
        _query.value = ""
        if (_browseAllCitiesMode.value && _fullCityCatalog.value.isNotEmpty()) {
            _cities.value = _fullCityCatalog.value.sortedBy { it.name }
        } else {
            _cities.value = emptyList()
        }
        _error.value = null
    }
}
