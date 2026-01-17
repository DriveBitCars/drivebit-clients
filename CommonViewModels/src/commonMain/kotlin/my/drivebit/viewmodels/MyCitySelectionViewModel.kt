package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.City
import my.drivebit.repositories.MyCityRepository

sealed interface MyCitySelectionState {
    data object Loading : MyCitySelectionState

    data class Error(
        val message: String,
    ) : MyCitySelectionState

    data class Success(
        val cities: List<City>,
    ) : MyCitySelectionState
}

sealed interface MyCitySelectionIntent {
    data class SearchCities(
        val query: String,
    ) : MyCitySelectionIntent

    data class SelectCity(
        val cityId: Int,
        val cityName: String,
    ) : MyCitySelectionIntent
}

interface MyCitySelectionViewModel {
    val state: StateFlow<MyCitySelectionState>

    fun handleIntent(intent: MyCitySelectionIntent)
}

class MyCitySelectionViewModelImpl(
    private val myCityRepository: MyCityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyCitySelectionViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<MyCitySelectionState>(MyCitySelectionState.Loading)

    override val state: StateFlow<MyCitySelectionState> = _state.asStateFlow()

    override fun handleIntent(intent: MyCitySelectionIntent) {
        when (intent) {
            is MyCitySelectionIntent.SearchCities -> {
                searchCities(intent.query)
            }
            is MyCitySelectionIntent.SelectCity -> {
                selectCity(intent.cityId, intent.cityName)
            }
        }
    }

    private fun searchCities(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _state.update { MyCitySelectionState.Success(emptyList()) }
                return@launch
            }

            _state.update { MyCitySelectionState.Loading }
            runCatching {
                myCityRepository.searchCities(query)
            }.onSuccess { cities ->
                _state.update { MyCitySelectionState.Success(cities) }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось найти города",
                    )
                _state.update { MyCitySelectionState.Error(errorMessage) }
            }
        }
    }

    private fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        myCityRepository.selectCity(cityId, cityName)
    }
}
