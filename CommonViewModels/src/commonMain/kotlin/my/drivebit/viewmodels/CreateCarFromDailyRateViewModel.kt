package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.CreateCarRepository
import my.drivebit.repositories.HasPassportRepo

sealed interface CreateCarFromDailyRateState {
    data object Idle : CreateCarFromDailyRateState

    data object Loading : CreateCarFromDailyRateState

    data object MissingPassport : CreateCarFromDailyRateState

    data class Error(
        val message: String,
    ) : CreateCarFromDailyRateState

    data class Success(
        val carId: String,
    ) : CreateCarFromDailyRateState
}

interface CreateCarFromDailyRateViewModel {
    val state: StateFlow<CreateCarFromDailyRateState>

    fun submitDailyRate(rate: Double)

    fun createFromSavedDailyRate()

    fun reset()
}

class CreateCarFromDailyRateViewModelImpl(
    private val carDataRepository: CarDataRepository,
    private val createCarRepository: CreateCarRepository,
    private val hasPassportRepo: HasPassportRepo,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CreateCarFromDailyRateViewModel {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow<CreateCarFromDailyRateState>(CreateCarFromDailyRateState.Idle)

    override val state: StateFlow<CreateCarFromDailyRateState>
        get() = _state.asStateFlow()

    override fun reset() {
        _state.update { CreateCarFromDailyRateState.Idle }
    }

    override fun createFromSavedDailyRate() {
        val rate = carDataRepository.getDailyRate() ?: return
        submitDailyRate(rate)
    }

    override fun submitDailyRate(rate: Double) {
        viewModelScope.launch {
            carDataRepository.saveDailyRate(rate)
            _state.update { CreateCarFromDailyRateState.Loading }

            val hasPassport =
                runCatching { hasPassportRepo.hasPasport() }
                    .getOrDefault(false)
            if (!hasPassport) {
                _state.update { CreateCarFromDailyRateState.MissingPassport }
                return@launch
            }

            createCarRepository
                .createCar(rate)
                .onSuccess { finalResponse ->
                    _state.update { CreateCarFromDailyRateState.Success(finalResponse.id) }
                }.onFailure { e ->
                    val message =
                        ErrorHandler.extractErrorMessage(
                            exception = e,
                            defaultNetworkError = "Ошибка сети",
                            defaultGenericError = "Не удалось создать автомобиль",
                        )
                    _state.update { CreateCarFromDailyRateState.Error(message) }
                }
        }
    }
}
