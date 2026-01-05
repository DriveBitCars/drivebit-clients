package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.MyCarRepository
import my.drivebit.utils.safeLaunchWithErrorHandler

interface MyCarsViewModel {
    val cars: StateFlow<List<CarItem>>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun loadCars()
}

class MyCarsViewModelImpl(
    private val myCarRepository: MyCarRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyCarsViewModel {
    private val _cars = MutableStateFlow<List<CarItem>>(emptyList())
    override val cars: StateFlow<List<CarItem>> = _cars.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    init {
        println("🏗️ [MyCarsViewModel] Instance created (hashCode: ${hashCode()})")
    }

    override fun loadCars() {
        if (_isLoading.value) {
            println("⏸️ [MyCarsViewModel] loadCars() skipped - already loading")
            return
        }

        println("🔄 [MyCarsViewModel] loadCars() called")
        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { _isLoading.value },
            setLoading = { _isLoading.value = it },
            setError = { _error.value = it },
            errorHandler = { e ->
                ErrorHandler.extractErrorMessage(
                    exception = e,
                    defaultNetworkError = "Ошибка сети",
                    defaultGenericError = "Не удалось загрузить список автомобилей",
                )
            },
        ) {
            myCarRepository.refresh()
            _cars.value = myCarRepository.getMyCar()
        }
    }
}
