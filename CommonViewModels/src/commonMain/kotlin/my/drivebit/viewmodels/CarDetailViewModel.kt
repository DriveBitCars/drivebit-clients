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
import my.drivebit.network.services.CarAvailability
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.Photo
import my.drivebit.utils.parseDisabledDatesFromIsoIntervals
import kotlin.runCatching

sealed interface CarDetailState {
    data object Loading : CarDetailState

    data class Success(
        val car: CarDetailResponse,
        val owner: CarOwnerUi? = null,
        val disabledBookingDates: Set<String> = emptySet(),
    ) : CarDetailState

    data class Error(
        val message: String,
    ) : CarDetailState
}

data class CarOwnerUi(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val memberSince: String?,
)

interface CarDetailViewModel {
    val state: StateFlow<CarDetailState>
}

class CarDetailViewModelImpl(
    private val carService: Car,
    private val photoService: Photo,
    private val carAvailability: CarAvailability,
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
                loadOwner(
                    ownerId = car.general.owner,
                    ownerName = car.general.ownerName,
                )
                loadBookingBlocks()
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

    private fun loadBookingBlocks() {
        if (carId.isBlank()) {
            return
        }
        viewModelScope.launch {
            val disabled =
                runCatching {
                    val blocks = carAvailability.getBlocks(carId)
                    parseDisabledDatesFromIsoIntervals(blocks.map { it.startAt to it.endAt })
                }.getOrDefault(emptySet())
            _state.update { current ->
                if (current is CarDetailState.Success) {
                    current.copy(disabledBookingDates = disabled)
                } else {
                    current
                }
            }
        }
    }

    private fun loadOwner(
        ownerId: String?,
        ownerName: String?,
    ) {
        if (ownerId.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            val owner =
                runCatching {
                    val avatarFromService = photoService.getAvatarByUserId(ownerId)?.url

                    CarOwnerUi(
                        id = ownerId,
                        name = buildOwnerName(ownerName),
                        avatarUrl = avatarFromService,
                        memberSince = null,
                    )
                }.getOrElse {
                    CarOwnerUi(
                        id = ownerId,
                        name = buildOwnerName(ownerName),
                        avatarUrl = null,
                        memberSince = null,
                    )
                }

            _state.update { current ->
                if (current is CarDetailState.Success) {
                    current.copy(owner = owner)
                } else {
                    current
                }
            }
        }
    }

    private fun buildOwnerName(ownerName: String?): String = ownerName?.takeIf { it.isNotBlank() } ?: "Владелец"
}
