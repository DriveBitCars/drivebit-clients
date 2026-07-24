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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository

sealed interface MainContentListState {
    data object Loading : MainContentListState

    data class FirstList(
        val cars: List<CarItem>,
        val totalCount: Int,
        val totalPages: Int,
    ) : MainContentListState

    data class Error(
        val message: String,
    ) : MainContentListState
}

interface MainContentViewModel {
    val firstList: StateFlow<MainContentListState>
    val displayedCars: StateFlow<List<CarItem>>
    val paginationInfo: StateFlow<Triple<Int, Int, Int>>

    fun refresh()

    fun setPage(page: Int)

    fun markSearchStarted()
}

class MainContentViewModelImpl(
    private val carSearchRepository: CarSearchRepository,
    private val onNavigatePage: ((Int) -> Unit)? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MainContentViewModel {
    private val viewModelScope = coroutineScope

    private val _firstList = MutableStateFlow<MainContentListState>(MainContentListState.Loading)

    override val firstList: StateFlow<MainContentListState> = _firstList.asStateFlow()

    override val displayedCars: StateFlow<List<CarItem>> =
        _firstList
            .map { state ->
                when (val s = state) {
                    is MainContentListState.FirstList -> s.cars
                    else -> emptyList()
                }
            }.stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    override val paginationInfo: StateFlow<Triple<Int, Int, Int>> =
        combine(
            carSearchRepository.currentPage,
            carSearchRepository.searchCarsByUserCity,
            _firstList,
        ) { page, response, listState ->
            if (listState is MainContentListState.FirstList) {
                Triple(page, response.totalPages, response.totalCount)
            } else {
                Triple(0, 0, 0)
            }
        }.catch { emit(Triple(0, 0, 0)) }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
                initialValue = Triple(0, 0, 0),
            )

    private var searchJob: Job? = null

    init {
        startSearchCollection()
    }

    private fun startSearchCollection() {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                carSearchRepository
                    .searchCarsByUserCity
                    .catch { e ->
                        val errorMessage =
                            ErrorHandler.extractErrorMessage(
                                exception = e,
                                defaultNetworkError = "Ошибка сети",
                                defaultGenericError = "Не удалось загрузить автомобили",
                            )
                        _firstList.value = MainContentListState.Error(errorMessage)
                    }.collectLatest { response ->
                        _firstList.value =
                            MainContentListState.FirstList(
                                cars = response.cars,
                                totalCount = response.totalCount,
                                totalPages = response.totalPages,
                            )
                    }
            }
    }

    override fun refresh() {
        markSearchStarted()
        carSearchRepository.refreshSearch()
    }

    override fun setPage(page: Int) {
        markSearchStarted()
        val safePage = page.coerceAtLeast(0)
        val navigate = onNavigatePage
        if (navigate != null) {
            navigate(safePage)
        } else {
            carSearchRepository.setPage(safePage)
        }
    }

    override fun markSearchStarted() {
        _firstList.value = MainContentListState.Loading
    }
}
