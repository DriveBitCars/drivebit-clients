package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.repositories.MyCarRepository
import my.drivebit.shared.storage.Storage

enum class CarMenuOption {
    ListYourCar,
    MyCars,
}

interface CarMenuViewModel {
    val menuOption: StateFlow<CarMenuOption>
    val isLoading: StateFlow<Boolean>

    fun load()
}

class CarMenuViewModelImpl(
    private val storage: Storage,
    private val myCarRepository: MyCarRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarMenuViewModel {
    private val _menuOption = MutableStateFlow<CarMenuOption>(CarMenuOption.ListYourCar)
    private val _isLoading = MutableStateFlow<Boolean>(false)

    init {
        println("🏗️ [CarMenuViewModel] Instance created (hashCode: ${hashCode()})")
    }

    override val menuOption: StateFlow<CarMenuOption>
        get() = _menuOption.asStateFlow()

    override val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    override fun load() {
        if (!storage.isLogined()) {
            _isLoading.value = false
            _menuOption.value = CarMenuOption.ListYourCar
            return
        }

        if (_isLoading.value) {
            println("⏸️ [CarMenuViewModel] load() skipped - already loading")
            return
        }

        println("🔄 [CarMenuViewModel] load() called")
        _isLoading.value = true
        coroutineScope.launch {
            runCatching {
                val cars = myCarRepository.getMyCar()
                _menuOption.value =
                    if (cars.isEmpty()) {
                        CarMenuOption.ListYourCar
                    } else {
                        CarMenuOption.MyCars
                    }
            }.onFailure {
                _menuOption.value = CarMenuOption.ListYourCar
            }.also {
                _isLoading.value = false
            }
        }
    }
}
