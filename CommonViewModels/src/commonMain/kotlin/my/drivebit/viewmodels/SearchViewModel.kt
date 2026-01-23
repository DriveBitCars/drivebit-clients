package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.FilterSuggestion
import my.drivebit.repositories.CarSearchRepository
import my.drivebit.repositories.MyCityRepository
import kotlin.runCatching

sealed interface SearchState {
    data object Loading : SearchState

    data class FiltersLoaded(
        val suggestedFilters: List<FilterSuggestion>,
    ) : SearchState

    data class Searching(
        val suggestedFilters: List<FilterSuggestion>,
    ) : SearchState

    data class SearchResults(
        val suggestedFilters: List<FilterSuggestion>,
        val cars: List<CarItem>,
    ) : SearchState

    data class Error(
        val message: String,
    ) : SearchState
}

interface SearchViewModel {
    val state: StateFlow<SearchState>

    fun loadSuggestedFilters()

    fun searchWithFilter(filter: FilterSuggestion)
}

class SearchViewModelImpl(
    private val dictionary: Dictionary,
    private val carService: Car,
    private val myCityRepository: MyCityRepository,
    private val carSearchRepository: CarSearchRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SearchViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<SearchState>(SearchState.Loading)

    override val state: StateFlow<SearchState>
        get() = _state.asStateFlow()

    init {
        loadSuggestedFilters()
    }

    override fun loadSuggestedFilters() {
        viewModelScope.launch {
            _state.update { SearchState.Loading }
            runCatching {
                dictionary.getFiltersSuggested()
            }.onSuccess { filters ->
                _state.update {
                    SearchState.FiltersLoaded(suggestedFilters = filters)
                }
                performInitialSearch(filters)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить фильтры",
                    )
                _state.update { SearchState.Error(errorMessage) }
            }
        }
    }

    private fun performInitialSearch(filters: List<FilterSuggestion>) {
        viewModelScope.launch {
            _state.update { SearchState.Searching(suggestedFilters = filters) }
            runCatching {
                carSearchRepository.searchCarsByUserCity()
            }.onSuccess { response ->
                _state.update {
                    SearchState.SearchResults(
                        suggestedFilters = filters,
                        cars = response.cars,
                    )
                }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось выполнить поиск",
                    )
                _state.update { SearchState.Error(errorMessage) }
            }
        }
    }

    override fun searchWithFilter(filter: FilterSuggestion) {
        viewModelScope.launch {
            val currentState = _state.value
            val filters =
                when (currentState) {
                    is SearchState.FiltersLoaded -> currentState.suggestedFilters
                    is SearchState.SearchResults -> currentState.suggestedFilters
                    else -> emptyList()
                }

            _state.update { SearchState.Searching(suggestedFilters = filters) }

            runCatching {
                val city = myCityRepository.getSelectedCity()
                carService.search(
                    cityId = city.id.toString(),
                    dateFrom = null,
                    dateTo = null,
                    availableMileagePerDayKmMin = filter.availableMileagePerDayKmMin,
                    dailyPriceMin = filter.dailyPriceMin,
                    dailyPriceMax = filter.dailyPriceMax,
                    yearMin = filter.yearMin,
                    yearMax = filter.yearMax,
                    seatsMin = filter.seatsMin,
                    seatsMax = filter.seatsMax,
                    bodyTypes = filter.getBodyTypeNames(),
                    engineTypes = filter.getEngineTypeNames(),
                    colors = filter.getColorNames(),
                )
            }.onSuccess { response ->
                _state.update {
                    SearchState.SearchResults(
                        suggestedFilters = filters,
                        cars = response.cars,
                    )
                }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось выполнить поиск",
                    )
                _state.update { SearchState.Error(errorMessage) }
            }
        }
    }
}
