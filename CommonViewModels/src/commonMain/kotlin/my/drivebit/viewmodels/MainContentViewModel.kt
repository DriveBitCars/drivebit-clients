package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val _firstList = MutableStateFlow<List<CarItem>>(emptyList())

    override val firstList: StateFlow<List<CarItem>> = _firstList.asStateFlow()

    private var isLoading = false

    init {
        loadCars()
    }

    private fun loadCars() {
        if (isLoading) {
            return
        }

        isLoading = true
        coroutineScope.launch {
            try {
                runCatching {
                    carSearchRepository.searchCarsByUserCity()
                }.onSuccess { response ->
                    _firstList.value = response.cars
                }.onFailure {
                    _firstList.value = emptyList()
                }
            } finally {
                isLoading = false
            }
        }
    }

    override fun refresh() {
        loadCars()
    }
}
