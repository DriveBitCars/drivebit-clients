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
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarResponse
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.HasPassportRepo
import my.drivebit.repositories.LicensePlateRepository
import my.drivebit.repositories.MyCarRepository
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedBodyTypeRepository
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.repositories.SelectedCarModelRepository
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.repositories.SelectedDriveTypeRepository
import my.drivebit.repositories.SelectedEngineTypeRepository

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
    private val selectedCarBrandRepository: SelectedCarBrandRepository,
    private val selectedCarModelRepository: SelectedCarModelRepository,
    private val selectedBodyTypeRepository: SelectedBodyTypeRepository,
    private val selectedDriveTypeRepository: SelectedDriveTypeRepository,
    private val selectedEngineTypeRepository: SelectedEngineTypeRepository,
    private val licensePlateRepository: LicensePlateRepository,
    private val selectedAddressRepository: SelectedAddressRepository,
    private val selectedCityRepository: SelectedCityRepository,
    private val myCarRepository: MyCarRepository,
    private val carService: Car,
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

            val request =
                CarCreateRequest(
                    brandId = selectedCarBrandRepository.getBrandId(),
                    modelId = selectedCarModelRepository.getModelId(),
                    bodyType = selectedBodyTypeRepository.getBodyTypeName(),
                    driveType = selectedDriveTypeRepository.getDriveTypeName(),
                    engineType = selectedEngineTypeRepository.getEngineTypeName(),
                    engineVolume = carDataRepository.getEngineVolume(),
                    productionYear = carDataRepository.getProductionYear(),
                    seatsCount = carDataRepository.getSeatsCount(),
                    licensePlate = licensePlateRepository.getLicensePlate(),
                    ValidAddressString = selectedAddressRepository.getAddress() ?: "",
                    cityId = selectedCityRepository.getCityId()?.toString(),
                    addr = null,
                    hourlyRate = 0.0,
                    dailyRate = rate,
                    ParkingAssistances = emptyList(),
                    MultimediaSystemOptions = emptyList(),
                )

            runCatching {
                val response = carService.createCar(request)
                ensureCarId(response)
            }.onSuccess { finalResponse ->
                myCarRepository.refresh()
                carDataRepository.saveCarId(finalResponse.id)
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

    private suspend fun ensureCarId(response: CarResponse): CarResponse {
        if (response.id.isNotBlank()) return response

        myCarRepository.refresh()
        val cars = myCarRepository.getMyCar()
        val firstCarId = cars.firstOrNull()?.id
        return if (!firstCarId.isNullOrBlank()) {
            CarResponse(id = firstCarId)
        } else {
            response
        }
    }
}
