package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository

private const val PAGE_SIZE = 12

interface MainContentViewModel {
    val firstList: StateFlow<List<CarItem>>
    val displayedCars: StateFlow<List<CarItem>>
    val paginationInfo: StateFlow<Triple<Int, Int, Int>>

    fun refresh()
    fun setPage(page: Int)
}

class MainContentViewModelImpl(
    private val carSearchRepository: CarSearchRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MainContentViewModel {
    override val firstList: StateFlow<List<CarItem>> =
        carSearchRepository
            .searchCarsByUserCity
            .map { it.cars }
            .catch { emit(emptyList()) }
            .stateIn(
                scope = coroutineScope,
                started = kotlinx.coroutines.flow.SharingStarted.Lazily,
                initialValue = emptyList(),
            )

    private val _currentPage = MutableStateFlow(0)

    init {
        firstList
            .onEach { _currentPage.value = 0 }
            .launchIn(coroutineScope)
    }

    override val displayedCars: StateFlow<List<CarItem>> =
        combine(firstList, _currentPage) { cars, page ->
            val totalPages = (cars.size + PAGE_SIZE - 1) / PAGE_SIZE
            val clampedPage = if (totalPages > 0) page.coerceIn(0, totalPages - 1) else 0
            cars.drop(clampedPage * PAGE_SIZE).take(PAGE_SIZE)
        }.stateIn(
            scope = coroutineScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    override val paginationInfo: StateFlow<Triple<Int, Int, Int>> =
        combine(firstList, _currentPage) { cars, page ->
            val totalPages = (cars.size + PAGE_SIZE - 1) / PAGE_SIZE
            val clampedPage = if (totalPages > 0) page.coerceIn(0, totalPages - 1) else 0
            Triple(clampedPage, totalPages, cars.size)
        }.stateIn(
            scope = coroutineScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = Triple(0, 0, 0),
        )

    override fun refresh() {
    }

    override fun setPage(page: Int) {
        _currentPage.value = page.coerceAtLeast(0)
    }
}
