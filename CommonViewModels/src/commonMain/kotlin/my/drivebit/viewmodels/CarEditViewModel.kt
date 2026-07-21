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
import my.drivebit.network.services.CarDetailResponse

sealed interface CarEditState {
    data object Loading : CarEditState

    data class Error(
        val message: String,
    ) : CarEditState

    data class Success(
        val formData: CarEditFormData,
        val isSaving: Boolean = false,
        val saveError: String? = null,
    ) : CarEditState
}

data class CarEditFormData(
    val carId: String,
    val licensePlate: String = "",
    val brandId: Int? = null,
    val brandName: String = "",
    val brandSearch: String = "",
    val modelId: Int? = null,
    val modelName: String = "",
    val modelSearch: String = "",
    val bodyType: String? = null,
    val bodyTypeTranslate: String = "",
    val bodyTypeSearch: String = "",
    val driveType: String? = null,
    val driveTypeTranslate: String = "",
    val driveTypeSearch: String = "",
    val engineType: String? = null,
    val engineTypeTranslate: String = "",
    val engineTypeSearch: String = "",
    val engineVolume: String = "",
    val productionYear: String = "",
    val seatsCount: String = "",
    val trunkSize: String? = null,
    val trunkSizeTranslate: String = "",
    val trunkSizeSearch: String = "",
    val address: String = "",
    val description: String = "",
    val hourlyRate: String = "",
    val dailyRate: String = "",
    val dailyRate4Days: String = "",
    val dailyRate7Days: String = "",
    val dailyRate14Days: String = "",
    val dailyRate21Days: String = "",
    val deposit: String = "",
    val availableMileagePerDayKm: String = "",
    val insurance: String? = null,
    val insuranceTranslate: String? = null,
    val allowedTravelDestinations: List<String> = emptyList(),
    val allowedTravelDestinationsTranslate: List<String> = emptyList(),
    val photos: List<my.drivebit.network.services.CarPhotoItem> = emptyList(),
)

interface CarEditViewModel {
    val state: StateFlow<CarEditState>
    val hasChanges: StateFlow<Boolean>

    fun loadCar(carId: String)

    fun updateLicensePlate(value: String)

    fun updateBrandSearch(value: String)

    fun selectBrand(
        brandId: Int,
        brandName: String,
    )

    fun updateModelSearch(value: String)

    fun selectModel(
        modelId: Int,
        modelName: String,
    )

    fun updateBodyTypeSearch(value: String)

    fun selectBodyType(
        bodyType: String,
        translate: String,
    )

    fun updateDriveTypeSearch(value: String)

    fun selectDriveType(
        driveType: String,
        translate: String,
    )

    fun updateEngineTypeSearch(value: String)

    fun selectEngineType(
        engineType: String,
        translate: String,
    )

    fun updateTrunkSizeSearch(value: String)

    fun selectTrunkSize(
        trunkSize: String,
        translate: String,
    )

    fun updateEngineVolume(value: String)

    fun updateProductionYear(value: String)

    fun updateSeatsCount(value: String)

    fun updateAddress(value: String)

    fun saveCar()
}

class CarEditViewModelImpl(
    private val carService: Car,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarEditViewModel {
    private val _state = MutableStateFlow<CarEditState>(CarEditState.Loading)
    private val _hasChanges = MutableStateFlow<Boolean>(false)
    private var isLoadingInProgress = false
    private var originalFormData: CarEditFormData? = null

    override val state: StateFlow<CarEditState>
        get() = _state.asStateFlow()

    override val hasChanges: StateFlow<Boolean>
        get() = _hasChanges.asStateFlow()

    override fun loadCar(carId: String) {
        if (isLoadingInProgress) return

        coroutineScope.launch {
            isLoadingInProgress = true
            _state.value = CarEditState.Loading
            runCatching {
                val car = carService.getCar(carId)
                val formData = mapToFormData(car)
                originalFormData = formData
                _hasChanges.value = false
                _state.value = CarEditState.Success(formData)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить данные автомобиля",
                    )
                _state.value = CarEditState.Error(errorMessage)
            }.also {
                isLoadingInProgress = false
            }
        }
    }

    internal fun mapToFormData(car: CarDetailResponse): CarEditFormData {
        val resolvedBodyType = car.resolvedBodyType()
        val resolvedDriveType = car.resolvedDriveType()
        val resolvedEngineType = car.resolvedEngineType()
        val resolvedEngineVolume = car.resolvedEngineVolume()
        val resolvedProductionYear = car.resolvedProductionYear()
        val resolvedSeatsCount = car.resolvedSeatsCount()
        val resolvedTrunkSize = car.resolvedTrunkSize()
        val resolvedTrunkSizeTranslate = car.resolvedTrunkSizeTranslate().orEmpty()

        return CarEditFormData(
            carId = car.id,
            licensePlate = car.resolvedLicensePlate(),
            brandId = car.resolvedBrandId(),
            brandName = car.resolvedBrandName(),
            brandSearch = car.resolvedBrandName(),
            modelId = car.resolvedModelId(),
            modelName = car.resolvedModelName(),
            modelSearch = car.resolvedModelName(),
            bodyType = resolvedBodyType.takeIf { it.isNotEmpty() },
            bodyTypeTranslate = car.resolvedBodyTypeTranslate(),
            bodyTypeSearch = car.resolvedBodyTypeTranslate(),
            driveType = resolvedDriveType.takeIf { it.isNotEmpty() },
            driveTypeTranslate = car.resolvedDriveTypeTranslate(),
            driveTypeSearch = car.resolvedDriveTypeTranslate(),
            engineType = resolvedEngineType.takeIf { it.isNotEmpty() },
            engineTypeTranslate = car.resolvedEngineTypeTranslate(),
            engineTypeSearch = car.resolvedEngineTypeTranslate(),
            engineVolume = if (resolvedEngineVolume > 0.0) NumberFormatter.formatDouble(resolvedEngineVolume) else "",
            productionYear = if (resolvedProductionYear > 0) NumberFormatter.formatInt(resolvedProductionYear) else "",
            seatsCount = if (resolvedSeatsCount > 0) NumberFormatter.formatInt(resolvedSeatsCount) else "",
            trunkSize = resolvedTrunkSize?.takeIf { it.isNotEmpty() },
            trunkSizeTranslate = resolvedTrunkSizeTranslate,
            trunkSizeSearch = resolvedTrunkSizeTranslate,
            address = car.resolvedAddressDisplay() ?: "",
            description = car.general.description ?: "",
            hourlyRate =
                car.hourlyRate
                    ?.toInt()
                    ?.takeIf { it > 0 }
                    ?.let { NumberFormatter.formatInt(it) } ?: "",
            dailyRate =
                car.dailyRate
                    ?.toInt()
                    ?.takeIf { it > 0 }
                    ?.let { NumberFormatter.formatInt(it) } ?: "",
            dailyRate4Days =
                car.dailyRate4Days?.toInt()?.takeIf { it > 0 }?.let {
                    NumberFormatter.formatInt(
                        it,
                    )
                } ?: "",
            dailyRate7Days =
                car.dailyRate7Days?.toInt()?.takeIf { it > 0 }?.let {
                    NumberFormatter.formatInt(
                        it,
                    )
                } ?: "",
            dailyRate14Days =
                car.dailyRate14Days
                    ?.toInt()
                    ?.takeIf { it > 0 }
                    ?.let { NumberFormatter.formatInt(it) } ?: "",
            dailyRate21Days =
                car.dailyRate21Days
                    ?.toInt()
                    ?.takeIf { it > 0 }
                    ?.let { NumberFormatter.formatInt(it) } ?: "",
            deposit =
                car.deposit
                    ?.toInt()
                    ?.takeIf { it > 0 }
                    ?.let { NumberFormatter.formatInt(it) } ?: "",
            availableMileagePerDayKm = car.availableMileagePerDayKm?.let { NumberFormatter.formatInt(it) } ?: "",
            insurance = car.insurance?.trim()?.takeIf { it.isNotEmpty() },
            insuranceTranslate = car.insuranceTranslate?.trim()?.takeIf { it.isNotEmpty() },
            allowedTravelDestinations = car.resolvedAllowedTravelDestinations(),
            allowedTravelDestinationsTranslate = car.resolvedAllowedTravelDestinationsTranslate(),
            photos = car.photos,
        )
    }

    override fun updateLicensePlate(value: String) {
        updateFormData { it.copy(licensePlate = value) }
    }

    override fun updateBrandSearch(value: String) {
        updateFormData { it.copy(brandSearch = value) }
    }

    override fun selectBrand(
        brandId: Int,
        brandName: String,
    ) {
        updateFormData { it.copy(brandId = brandId, brandName = brandName, brandSearch = brandName) }
    }

    override fun updateModelSearch(value: String) {
        updateFormData { it.copy(modelSearch = value) }
    }

    override fun selectModel(
        modelId: Int,
        modelName: String,
    ) {
        updateFormData { it.copy(modelId = modelId, modelName = modelName, modelSearch = modelName) }
    }

    override fun updateBodyTypeSearch(value: String) {
        updateFormData { it.copy(bodyTypeSearch = value) }
    }

    override fun selectBodyType(
        bodyType: String,
        translate: String,
    ) {
        updateFormData { it.copy(bodyType = bodyType, bodyTypeTranslate = translate, bodyTypeSearch = translate) }
    }

    override fun updateDriveTypeSearch(value: String) {
        updateFormData { it.copy(driveTypeSearch = value) }
    }

    override fun selectDriveType(
        driveType: String,
        translate: String,
    ) {
        updateFormData { it.copy(driveType = driveType, driveTypeTranslate = translate, driveTypeSearch = translate) }
    }

    override fun updateEngineTypeSearch(value: String) {
        updateFormData { it.copy(engineTypeSearch = value) }
    }

    override fun selectEngineType(
        engineType: String,
        translate: String,
    ) {
        updateFormData {
            it.copy(
                engineType = engineType,
                engineTypeTranslate = translate,
                engineTypeSearch = translate,
            )
        }
    }

    override fun updateTrunkSizeSearch(value: String) {
        updateFormData { it.copy(trunkSizeSearch = value) }
    }

    override fun selectTrunkSize(
        trunkSize: String,
        translate: String,
    ) {
        updateFormData {
            it.copy(
                trunkSize = trunkSize,
                trunkSizeTranslate = translate,
                trunkSizeSearch = translate,
            )
        }
    }

    override fun updateEngineVolume(value: String) {
        updateFormData { it.copy(engineVolume = value) }
    }

    override fun updateProductionYear(value: String) {
        updateFormData { it.copy(productionYear = value) }
    }

    override fun updateSeatsCount(value: String) {
        updateFormData { it.copy(seatsCount = value) }
    }

    override fun updateAddress(value: String) {
        updateFormData { it.copy(address = value) }
    }

    private fun updateFormData(update: (CarEditFormData) -> CarEditFormData) {
        _state.update { currentState ->
            when (currentState) {
                is CarEditState.Success -> {
                    val updatedFormData = update(currentState.formData)
                    val hasChanges =
                        originalFormData?.let { original ->
                            updatedFormData != original
                        } ?: false
                    _hasChanges.value = hasChanges
                    currentState.copy(formData = updatedFormData)
                }

                else -> currentState
            }
        }
    }

    override fun saveCar() {
        val currentState = _state.value
        if (currentState !is CarEditState.Success) return

        val formData = currentState.formData

        coroutineScope.launch {
            _state.update {
                when (it) {
                    is CarEditState.Success -> it.copy(isSaving = true, saveError = null)
                    else -> it
                }
            }
            runCatching {
                val request =
                    CarCreateRequest(
                        brandId = formData.brandId,
                        modelId = formData.modelId,
                        bodyType = formData.bodyType,
                        driveType = formData.driveType,
                        engineType = formData.engineType,
                        engineVolume = formData.engineVolume.toDoubleOrNull(),
                        year =
                            formData.productionYear
                                .toIntOrNull()
                                ?: throw IllegalArgumentException("Year is required"),
                        seats = formData.seatsCount.toIntOrNull(),
                        trunkSize = formData.trunkSize,
                        licensePlate = formData.licensePlate.takeIf { it.isNotBlank() },
                        description = formData.description.takeIf { it.isNotBlank() },
                        ValidAddressString = formData.address,
                        deposit = formData.deposit.takeIf { it.isNotBlank() }?.toIntOrNull(),
                        availableMileagePerDayKm =
                            formData.availableMileagePerDayKm.takeIf { it.isNotBlank() }?.toIntOrNull(),
                        insurance = formData.insurance?.trim()?.takeIf { it.isNotEmpty() },
                        ParkingAssistances = emptyList(),
                        MultimediaSystemOptions = emptyList(),
                        allowedTravelDestinations = formData.allowedTravelDestinations,
                    )
                carService.createOrUpdateCar(request, formData.carId)
                val updatedCar = carService.getCar(formData.carId)
                val updatedFormData = mapToFormData(updatedCar)
                originalFormData = updatedFormData
                _hasChanges.value = false
                _state.value = CarEditState.Success(updatedFormData, isSaving = false)
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось сохранить изменения",
                    )
                _state.update {
                    when (it) {
                        is CarEditState.Success -> it.copy(saveError = errorMessage, isSaving = false)
                        else -> it
                    }
                }
            }
        }
    }
}
