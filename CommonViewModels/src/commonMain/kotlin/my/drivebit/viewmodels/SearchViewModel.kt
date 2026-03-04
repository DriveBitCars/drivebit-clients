package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository
import my.drivebit.repositories.CurrentFiltersRepository

private const val PAGE_SIZE = 12

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
    val displayedCars: StateFlow<List<CarItem>>
    val paginationInfo: StateFlow<Triple<Int, Int, Int>>

    fun loadSearchResults()
    fun setPage(page: Int)

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

    fun updateDriveType(
        name: String?,
        translate: String?,
    )

    fun updateBodyType(
        name: String?,
        translate: String?,
    )

    fun updateSeatsMin(value: Int?)

    fun updateEngineType(
        name: String?,
        translate: String?,
    )

    fun updateColor(
        name: String?,
        translate: String?,
    )

    fun updateYearMin(value: Int?)

    fun updateYearMax(value: Int?)

    fun updateSeatsMax(value: Int?)

    fun updateAvailableMileagePerDayKmMin(value: Int?)
}

class SearchViewModelImpl(
    private val carSearchRepository: CarSearchRepository,
    private val currentFiltersRepository: CurrentFiltersRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : SearchViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<SearchState>(SearchState.Loading)
    private val _currentPage = MutableStateFlow(0)

    override val state: StateFlow<SearchState>
        get() = _state.asStateFlow()

    override val displayedCars: StateFlow<List<CarItem>> =
        combine(_state, _currentPage) { searchState, page ->
            when (val s = searchState) {
                is SearchState.SearchResults -> {
                    val totalPages = (s.cars.size + PAGE_SIZE - 1) / PAGE_SIZE
                    val clampedPage = if (totalPages > 0) page.coerceIn(0, totalPages - 1) else 0
                    s.cars.drop(clampedPage * PAGE_SIZE).take(PAGE_SIZE)
                }
                else -> emptyList()
            }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    override val paginationInfo: StateFlow<Triple<Int, Int, Int>> =
        combine(_state, _currentPage) { searchState, page ->
            when (val s = searchState) {
                is SearchState.SearchResults -> {
                    val totalPages = (s.cars.size + PAGE_SIZE - 1) / PAGE_SIZE
                    val clampedPage = if (totalPages > 0) page.coerceIn(0, totalPages - 1) else 0
                    Triple(clampedPage, totalPages, s.cars.size)
                }
                else -> Triple(0, 0, 0)
            }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = Triple(0, 0, 0),
        )

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
                        _currentPage.value = 0
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

    override fun updateDriveType(
        name: String?,
        translate: String?,
    ) {
        currentFiltersRepository.updateDriveType(name, translate)
    }

    override fun updateBodyType(
        name: String?,
        translate: String?,
    ) {
        currentFiltersRepository.updateBodyType(name, translate)
    }

    override fun updateSeatsMin(value: Int?) {
        currentFiltersRepository.updateSeatsMin(value)
    }

    override fun updateEngineType(
        name: String?,
        translate: String?,
    ) {
        currentFiltersRepository.updateEngineType(name, translate)
    }

    override fun updateColor(
        name: String?,
        translate: String?,
    ) {
        currentFiltersRepository.updateColor(name, translate)
    }

    override fun updateYearMin(value: Int?) {
        currentFiltersRepository.updateYearMin(value)
    }

    override fun updateYearMax(value: Int?) {
        currentFiltersRepository.updateYearMax(value)
    }

    override fun updateSeatsMax(value: Int?) {
        currentFiltersRepository.updateSeatsMax(value)
    }

    override fun updateAvailableMileagePerDayKmMin(value: Int?) {
        currentFiltersRepository.updateAvailableMileagePerDayKmMin(value)
    }

    override fun setPage(page: Int) {
        _currentPage.value = page.coerceAtLeast(0)
    }
}
