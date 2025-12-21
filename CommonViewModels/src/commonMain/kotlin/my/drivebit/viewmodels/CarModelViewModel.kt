package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarModel
import my.drivebit.repositories.CarModelRepository
import my.drivebit.repositories.ResultCarModels

class CarModelViewModel(
    private val carModelRepository: CarModelRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _allModels = MutableStateFlow<List<CarModel>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _models = MutableStateFlow<List<CarModel>>(emptyList())
    val models: StateFlow<List<CarModel>> = _models.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadModels(brandId: Int) {
        coroutineScope.launch {
            _error.value = null
            when (val result = carModelRepository.getModels(brandId)) {
                is ResultCarModels.Success -> {
                    _allModels.value = result.models
                    filterModels(_query.value)
                }
                is ResultCarModels.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        filterModels(newQuery)
    }

    private fun filterModels(query: String) {
        if (query.isBlank()) {
            _models.value = _allModels.value
        } else {
            val lowerQuery = query.lowercase()
            _models.value =
                _allModels.value.filter {
                    it.name.lowercase().contains(lowerQuery) ||
                        it.cyrillicName?.lowercase()?.contains(lowerQuery) == true
                }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _models.value = _allModels.value
    }
}
