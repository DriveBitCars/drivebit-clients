package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository

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

    override val displayedCars: StateFlow<List<CarItem>> = firstList

    override val paginationInfo: StateFlow<Triple<Int, Int, Int>> =
        combine(
            carSearchRepository.currentPage,
            carSearchRepository.searchCarsByUserCity,
        ) { page, response ->
            Triple(page, response.totalPages, response.totalCount)
        }.catch { emit(Triple(0, 0, 0)) }
            .stateIn(
                scope = coroutineScope,
                started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
                initialValue = Triple(0, 0, 0),
            )

    override fun refresh() {
    }

    override fun setPage(page: Int) {
        carSearchRepository.setPage(page.coerceAtLeast(0))
    }
}
