package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.CarAvailability
import my.drivebit.network.services.CarAvailabilityBlock
import my.drivebit.network.services.CreateAvailabilityBlockRequest
import my.drivebit.utils.availabilityBlockIntervalFromDates
import my.drivebit.utils.parseDisabledDatesFromIsoIntervals

sealed interface CarAvailabilityState {
    data object Loading : CarAvailabilityState

    data class Error(
        val message: String,
    ) : CarAvailabilityState

    data class Success(
        val blocks: List<CarAvailabilityBlock>,
        val disabledDates: Set<String>,
        val isSaving: Boolean = false,
        val formError: String? = null,
        val selectedStartDate: String? = null,
        val selectedEndDate: String? = null,
    ) : CarAvailabilityState
}

interface CarAvailabilityViewModel {
    val state: StateFlow<CarAvailabilityState>

    fun loadBlocks()

    fun updateSelectedStartDate(date: String?)

    fun updateSelectedEndDate(date: String?)

    fun createBlock()

    fun deleteBlock(blockId: String)
}

class CarAvailabilityViewModelImpl(
    private val carId: String,
    private val carAvailability: CarAvailability,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarAvailabilityViewModel {
    private val _state = MutableStateFlow<CarAvailabilityState>(CarAvailabilityState.Loading)
    private var isLoadingInProgress = false

    override val state: StateFlow<CarAvailabilityState>
        get() = _state.asStateFlow()

    init {
        loadBlocks()
    }

    override fun loadBlocks() {
        if (carId.isBlank()) {
            _state.value = CarAvailabilityState.Error("Не указан ID автомобиля")
            return
        }
        if (isLoadingInProgress) return

        coroutineScope.launch {
            isLoadingInProgress = true
            _state.value = CarAvailabilityState.Loading
            reloadBlocks(showLoading = true)
            isLoadingInProgress = false
        }
    }

    private suspend fun reloadBlocks(showLoading: Boolean = false) {
        runCatching {
            val blocks = carAvailability.getBlocks(carId)
            val disabledDates =
                parseDisabledDatesFromIsoIntervals(blocks.map { it.startAt to it.endAt })
            val previousSelection =
                (_state.value as? CarAvailabilityState.Success)?.let {
                    it.selectedStartDate to it.selectedEndDate
                }
            _state.value =
                CarAvailabilityState.Success(
                    blocks = blocks,
                    disabledDates = disabledDates,
                    selectedStartDate = previousSelection?.first,
                    selectedEndDate = previousSelection?.second,
                )
        }.onFailure { e ->
            if (showLoading || _state.value !is CarAvailabilityState.Success) {
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить календарь доступности",
                    )
                _state.value = CarAvailabilityState.Error(errorMessage)
            } else {
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось обновить календарь доступности",
                    )
                _state.update {
                    if (it is CarAvailabilityState.Success) {
                        it.copy(formError = errorMessage, isSaving = false)
                    } else {
                        it
                    }
                }
            }
        }
    }

    override fun updateSelectedStartDate(date: String?) {
        _state.update { current ->
            if (current is CarAvailabilityState.Success) {
                current.copy(selectedStartDate = date, formError = null)
            } else {
                current
            }
        }
    }

    override fun updateSelectedEndDate(date: String?) {
        _state.update { current ->
            if (current is CarAvailabilityState.Success) {
                current.copy(selectedEndDate = date, formError = null)
            } else {
                current
            }
        }
    }

    override fun createBlock() {
        val currentState = _state.value
        if (currentState !is CarAvailabilityState.Success) return

        val startDate = currentState.selectedStartDate
        val endDate = currentState.selectedEndDate ?: startDate
        if (startDate.isNullOrBlank()) {
            _state.update {
                if (it is CarAvailabilityState.Success) {
                    it.copy(formError = "Выберите даты для блокировки")
                } else {
                    it
                }
            }
            return
        }

        coroutineScope.launch {
            _state.update {
                when (it) {
                    is CarAvailabilityState.Success -> it.copy(isSaving = true, formError = null)
                    else -> it
                }
            }

            runCatching {
                val interval = availabilityBlockIntervalFromDates(startDate, endDate ?: startDate)
                carAvailability.createBlock(
                    CreateAvailabilityBlockRequest(
                        carId = carId,
                        startAt = interval.startAt,
                        endAt = interval.endAt,
                    ),
                )
                reloadBlocks()
                _state.update {
                    when (it) {
                        is CarAvailabilityState.Success ->
                            it.copy(
                                isSaving = false,
                                selectedStartDate = null,
                                selectedEndDate = null,
                                formError = null,
                            )
                        else -> it
                    }
                }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось заблокировать даты",
                    )
                _state.update {
                    when (it) {
                        is CarAvailabilityState.Success -> it.copy(isSaving = false, formError = errorMessage)
                        else -> it
                    }
                }
            }
        }
    }

    override fun deleteBlock(blockId: String) {
        if (blockId.isBlank()) return

        coroutineScope.launch {
            runCatching {
                carAvailability.deleteBlock(blockId)
                reloadBlocks()
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось удалить блокировку",
                    )
                _state.update {
                    when (it) {
                        is CarAvailabilityState.Success -> it.copy(formError = errorMessage)
                        else -> it
                    }
                }
            }
        }
    }
}
