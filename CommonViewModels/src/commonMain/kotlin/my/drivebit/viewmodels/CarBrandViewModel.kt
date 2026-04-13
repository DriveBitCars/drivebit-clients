package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarBrand
import my.drivebit.repositories.CarBrandRepository
import my.drivebit.repositories.ResultCarBrands

class CarBrandViewModel(
    private val carBrandRepository: CarBrandRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _allBrands = MutableStateFlow<List<CarBrand>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _brands = MutableStateFlow<List<CarBrand>>(emptyList())
    val brands: StateFlow<List<CarBrand>> = _brands.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadBrands(existingInFleetOnly: Boolean = false) {
        coroutineScope.launch {
            _error.value = null
            val result =
                if (existingInFleetOnly) {
                    carBrandRepository.getBrandsExistingInFleet()
                } else {
                    carBrandRepository.getBrands()
                }
            when (result) {
                is ResultCarBrands.Success -> {
                    _allBrands.value = result.brands
                    filterBrands(_query.value)
                }
                is ResultCarBrands.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        filterBrands(newQuery)
    }

    private fun filterBrands(query: String) {
        if (query.isBlank()) {
            _brands.value = _allBrands.value
        } else {
            val lowerQuery = query.lowercase()
            _brands.value =
                _allBrands.value.filter {
                    it.name.lowercase().contains(lowerQuery) ||
                        it.cyrillicName?.lowercase()?.contains(lowerQuery) == true
                }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _brands.value = _allBrands.value
    }
}
