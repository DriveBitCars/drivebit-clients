package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import my.drivebit.repositories.CurrentFiltersRepository

sealed interface SearchState {
    data object Loading : SearchState

    data object Searching : SearchState

    data class SearchResults(
        val cars: List<CarItem>,
    ) : SearchState

    data class Error(
        val message: String,
    ) : SearchState
}

interface SearchViewModel {
    val state: StateFlow<SearchState>

    fun loadSearchResults()

    fun updateDailyRateMin(value: Double?)

    fun updateDailyRateMax(value: Double?)

    fun updateBrand(
        id: Int?,
        name: String?,
    )

    fun updateModel(
        id: Int?,
        name: String?,
    )
}

class SearchViewModelImpl(
    private val carSearchRepository: CarSearchRepository,
    private val currentFiltersRepository: CurrentFiltersRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SearchViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<SearchState>(SearchState.Loading)

    override val state: StateFlow<SearchState>
        get() = _state.asStateFlow()

    init {
        loadSearchResults()
    }

    private var searchJob: Job? = null

    override fun loadSearchResults() {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update { SearchState.Searching }

                carSearchRepository
                    .searchCarsByUserCity
                    .catch { e ->
                        val errorMessage =
                            ErrorHandler.extractErrorMessage(
                                exception = e,
                                defaultNetworkError = "Ошибка сети",
                                defaultGenericError = "Не удалось выполнить поиск",
                            )
                        _state.update { SearchState.Error(errorMessage) }
                    }.collectLatest { response ->
                        _state.update {
                            SearchState.SearchResults(
                                cars = response.cars,
                            )
                        }
                    }
            }
    }

    override fun updateDailyRateMin(value: Double?) {
        currentFiltersRepository.updateDailyRateMin(value)
    }

    override fun updateDailyRateMax(value: Double?) {
        currentFiltersRepository.updateDailyRateMax(value)
    }

    override fun updateBrand(
        id: Int?,
        name: String?,
    ) {
        currentFiltersRepository.updateBrand(id, name)
    }

    override fun updateModel(
        id: Int?,
        name: String?,
    ) {
        currentFiltersRepository.updateModel(id, name)
    }
}
