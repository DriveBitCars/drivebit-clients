package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository

sealed interface CarSearchState {
    data object Idle : CarSearchState

    data object Loading : CarSearchState

    data class Success(
        val cars: List<CarItem>,
    ) : CarSearchState

    data class Error(
        val message: String,
    ) : CarSearchState
}

sealed interface CarSearchIntent {
    data object SearchCars : CarSearchIntent

    data class SearchCarsWithDates(
        val dateFrom: String?,
        val dateTo: String?,
    ) : CarSearchIntent
}

interface CarSearchViewModel {
    val state: StateFlow<CarSearchState>

    fun handleIntent(intent: CarSearchIntent)
}

class CarSearchViewModelImpl(
    private val carSearchRepository: CarSearchRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarSearchViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<CarSearchState>(CarSearchState.Idle)

    override val state: StateFlow<CarSearchState> = _state.asStateFlow()

    override fun handleIntent(intent: CarSearchIntent) {
        when (intent) {
            is CarSearchIntent.SearchCars,
            is CarSearchIntent.SearchCarsWithDates,
            -> searchCars()
        }
    }

    private fun searchCars() {
        viewModelScope.launch {
            _state.update { CarSearchState.Loading }
            carSearchRepository
                .searchCarsByUserCity
                .catch { e ->
                    val errorMessage =
                        ErrorHandler.extractErrorMessage(
                            exception = e,
                            defaultNetworkError = "Ошибка сети",
                            defaultGenericError = "Не удалось найти машины",
                        )
                    _state.update { CarSearchState.Error(errorMessage) }
                }.collectLatest { response ->
                    _state.update { CarSearchState.Success(response.cars) }
                }
        }
    }
}
