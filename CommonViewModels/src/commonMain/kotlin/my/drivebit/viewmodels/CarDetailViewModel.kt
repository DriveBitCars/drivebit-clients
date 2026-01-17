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
import my.drivebit.network.services.CarDetailResponse
import kotlin.runCatching

sealed interface CarDetailState {
    data object Loading : CarDetailState

    data class Success(
        val car: CarDetailResponse,
    ) : CarDetailState

    data class Error(
        val message: String,
    ) : CarDetailState
}

interface CarDetailViewModel {
    val state: StateFlow<CarDetailState>
}

class CarDetailViewModelImpl(
    private val carService: Car,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarDetailViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<CarDetailState>(CarDetailState.Loading)

    override val state: StateFlow<CarDetailState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { CarDetailState.Loading }
            runCatching {
                carService.getCar(carId)
            }.onSuccess { car ->
                _state.update { CarDetailState.Success(car) }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить информацию об автомобиле",
                    )
                _state.update { CarDetailState.Error(errorMessage) }
            }
        }
    }
}
