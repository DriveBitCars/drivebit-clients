package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CarSearchRepository

interface MainContentViewModel {
    val firstList: StateFlow<List<CarItem>>

    fun refresh()
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

    override fun refresh() {
    }
}
