package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarModel
import my.drivebit.repositories.EnumItem
import my.drivebit.repositories.MyCarRepository

sealed interface CarEditIntent {
    data class LoadCar(
        val carId: String,
    ) : CarEditIntent

    data object SaveCar : CarEditIntent

    data object DeleteCar : CarEditIntent

    data class UpdateLicensePlate(
        val value: String,
    ) : CarEditIntent

    data class UpdateBrandSearch(
        val value: String,
    ) : CarEditIntent

    data class SelectBrand(
        val brandId: Int,
        val brandName: String,
    ) : CarEditIntent

    data class UpdateModelSearch(
        val value: String,
    ) : CarEditIntent

    data class SelectModel(
        val modelId: Int,
        val modelName: String,
    ) : CarEditIntent

    data class UpdateBodyTypeSearch(
        val value: String,
    ) : CarEditIntent

    data class SelectBodyType(
        val bodyType: String,
        val translate: String,
    ) : CarEditIntent

    data class UpdateDriveTypeSearch(
        val value: String,
    ) : CarEditIntent

    data class SelectDriveType(
        val driveType: String,
        val translate: String,
    ) : CarEditIntent

    data class UpdateEngineTypeSearch(
        val value: String,
    ) : CarEditIntent

    data class SelectEngineType(
        val engineType: String,
        val translate: String,
    ) : CarEditIntent

    data class UpdateEngineVolume(
        val value: String,
    ) : CarEditIntent

    data class UpdateProductionYear(
        val value: String,
    ) : CarEditIntent

    data class UpdateSeatsCount(
        val value: String,
    ) : CarEditIntent

    data class UpdateHourlyRate(
        val value: String,
    ) : CarEditIntent

    data class UpdateDailyRate(
        val value: String,
    ) : CarEditIntent

    data class SetBrandFocus(
        val isFocused: Boolean,
    ) : CarEditIntent

    data class SetModelFocus(
        val isFocused: Boolean,
    ) : CarEditIntent

    data class SetBodyTypeFocus(
        val isFocused: Boolean,
    ) : CarEditIntent

    data class SetDriveTypeFocus(
        val isFocused: Boolean,
    ) : CarEditIntent

    data class SetEngineTypeFocus(
        val isFocused: Boolean,
    ) : CarEditIntent

    data class SetBrandBlurTimeout(
        val timeout: Int?,
    ) : CarEditIntent

    data class SetModelBlurTimeout(
        val timeout: Int?,
    ) : CarEditIntent

    data class SetBodyTypeBlurTimeout(
        val timeout: Int?,
    ) : CarEditIntent

    data class SetDriveTypeBlurTimeout(
        val timeout: Int?,
    ) : CarEditIntent

    data class SetEngineTypeBlurTimeout(
        val timeout: Int?,
    ) : CarEditIntent
}

sealed interface CarEditMviState {
    data object Loading : CarEditMviState

    data class Error(
        val message: String,
    ) : CarEditMviState

    data class Success(
        val carId: String,
        val formData: CarEditFormData,
        val brands: List<CarBrand> = emptyList(),
        val models: List<CarModel> = emptyList(),
        val bodyTypes: List<EnumItem> = emptyList(),
        val driveTypes: List<EnumItem> = emptyList(),
        val engineTypes: List<EnumItem> = emptyList(),
        val isBrandFocused: Boolean = false,
        val isModelFocused: Boolean = false,
        val isBodyTypeFocused: Boolean = false,
        val isDriveTypeFocused: Boolean = false,
        val isEngineTypeFocused: Boolean = false,
        val brandBlurTimeout: Int? = null,
        val modelBlurTimeout: Int? = null,
        val bodyTypeBlurTimeout: Int? = null,
        val driveTypeBlurTimeout: Int? = null,
        val engineTypeBlurTimeout: Int? = null,
        val saveError: String? = null,
        val hasChanges: Boolean = false,
    ) : CarEditMviState {
        val isSaveButtonEnabled: Boolean
            get() = hasChanges
    }

    data object NavigateToMyCars : CarEditMviState
}

interface CarEditMviViewModel {
    val state: StateFlow<CarEditMviState>
    val hasChanges: StateFlow<Boolean>

    fun handleIntent(intent: CarEditIntent)
}

class CarEditMviViewModelImpl(
    private val carService: Car,
    private val myCarRepository: MyCarRepository,
    private val carBrandViewModel: CarBrandViewModel,
    private val carModelViewModel: CarModelViewModel,
    private val bodyTypeViewModel: BodyTypeViewModel,
    private val driveTypeViewModel: DriveTypeViewModel,
    private val engineTypeViewModel: EngineTypeViewModel,
    private val carId: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val observerCoroutineScope: CoroutineScope? = null,
) : CarEditMviViewModel {
    private val viewModelScope = coroutineScope
    private val observerScope = observerCoroutineScope ?: viewModelScope

    private val _state = MutableStateFlow<CarEditMviState>(CarEditMviState.Loading)
    override val state: StateFlow<CarEditMviState> = _state.asStateFlow()

    private val _hasChanges = MutableStateFlow<Boolean>(false)
    override val hasChanges: StateFlow<Boolean> = _hasChanges.asStateFlow()

    init {
        observeViewModels()
        carId.let { loadCar(it) }
    }

    private fun observeViewModels() {
        observerScope.launch {
            combine(
                carBrandViewModel.brands,
                carModelViewModel.models,
                bodyTypeViewModel.bodyTypes,
                driveTypeViewModel.driveTypes,
                engineTypeViewModel.engineTypes,
            ) { brands, models, bodyTypes, driveTypes, engineTypes ->
                _state.update { currentState ->
                    when (currentState) {
                        is CarEditMviState.Success -> {
                            val updatedFormData = resolveBrandAndModelIds(currentState.formData, brands, models)
                            currentState.copy(
                                formData = updatedFormData,
                                brands = brands,
                                models = models,
                                bodyTypes = bodyTypes,
                                driveTypes = driveTypes,
                                engineTypes = engineTypes,
                            )
                        }
                        else -> currentState
                    }
                }
            }.collect {}
        }
    }

    private fun resolveBrandAndModelIds(
        formData: CarEditFormData,
        brands: List<CarBrand>,
        models: List<CarModel>,
    ): CarEditFormData {
        var updatedFormData = formData

        if (formData.brandId == null && formData.brandName.isNotBlank()) {
            val brand = brands.find { it.name == formData.brandName || it.cyrillicName == formData.brandName }
            if (brand != null) {
                updatedFormData = updatedFormData.copy(brandId = brand.id)
            }
        }

        if (updatedFormData.modelId == null &&
            updatedFormData.modelName.isNotBlank() &&
            updatedFormData.brandId != null
        ) {
            val model =
                models.find {
                    it.name == updatedFormData.modelName ||
                        it.cyrillicName == updatedFormData.modelName
                }
            if (model != null) {
                updatedFormData = updatedFormData.copy(modelId = model.id)
            }
        }

        return updatedFormData
    }

    override fun handleIntent(intent: CarEditIntent) {
        when (intent) {
            is CarEditIntent.LoadCar -> {
                loadCar(intent.carId)
            }
            is CarEditIntent.SaveCar -> {
                saveCar()
            }
            is CarEditIntent.DeleteCar -> {
                deleteCar()
            }
            is CarEditIntent.UpdateLicensePlate -> {
                updateFormData { it.copy(licensePlate = intent.value) }
            }
            is CarEditIntent.UpdateBrandSearch -> {
                carBrandViewModel.updateQuery(intent.value)
            }
            is CarEditIntent.SelectBrand -> {
                selectBrand(intent.brandId, intent.brandName)
            }
            is CarEditIntent.UpdateModelSearch -> {
                carModelViewModel.updateQuery(intent.value)
            }
            is CarEditIntent.SelectModel -> {
                selectModel(intent.modelId, intent.modelName)
            }
            is CarEditIntent.UpdateBodyTypeSearch -> {
                updateFormData { it.copy(bodyTypeSearch = intent.value) }
            }
            is CarEditIntent.SelectBodyType -> {
                selectBodyType(intent.bodyType, intent.translate)
            }
            is CarEditIntent.UpdateDriveTypeSearch -> {
                updateFormData { it.copy(driveTypeSearch = intent.value) }
            }
            is CarEditIntent.SelectDriveType -> {
                selectDriveType(intent.driveType, intent.translate)
            }
            is CarEditIntent.UpdateEngineTypeSearch -> {
                updateFormData { it.copy(engineTypeSearch = intent.value) }
            }
            is CarEditIntent.SelectEngineType -> {
                selectEngineType(intent.engineType, intent.translate)
            }
            is CarEditIntent.UpdateEngineVolume -> {
                updateFormData { it.copy(engineVolume = intent.value) }
            }
            is CarEditIntent.UpdateProductionYear -> {
                updateFormData { it.copy(productionYear = intent.value) }
            }
            is CarEditIntent.UpdateSeatsCount -> {
                updateFormData { it.copy(seatsCount = intent.value) }
            }
            is CarEditIntent.UpdateHourlyRate -> {
                updateFormData { it.copy(hourlyRate = intent.value) }
            }
            is CarEditIntent.UpdateDailyRate -> {
                updateFormData { it.copy(dailyRate = intent.value) }
            }
            is CarEditIntent.SetBrandFocus -> {
                updateFocusState { it.copy(isBrandFocused = intent.isFocused) }
            }
            is CarEditIntent.SetModelFocus -> {
                updateFocusState { it.copy(isModelFocused = intent.isFocused) }
            }
            is CarEditIntent.SetBodyTypeFocus -> {
                updateFocusState { it.copy(isBodyTypeFocused = intent.isFocused) }
            }
            is CarEditIntent.SetDriveTypeFocus -> {
                updateFocusState { it.copy(isDriveTypeFocused = intent.isFocused) }
            }
            is CarEditIntent.SetEngineTypeFocus -> {
                updateFocusState { it.copy(isEngineTypeFocused = intent.isFocused) }
            }
            is CarEditIntent.SetBrandBlurTimeout -> {
                updateFocusState { it.copy(brandBlurTimeout = intent.timeout) }
            }
            is CarEditIntent.SetModelBlurTimeout -> {
                updateFocusState { it.copy(modelBlurTimeout = intent.timeout) }
            }
            is CarEditIntent.SetBodyTypeBlurTimeout -> {
                updateFocusState { it.copy(bodyTypeBlurTimeout = intent.timeout) }
            }
            is CarEditIntent.SetDriveTypeBlurTimeout -> {
                updateFocusState { it.copy(driveTypeBlurTimeout = intent.timeout) }
            }
            is CarEditIntent.SetEngineTypeBlurTimeout -> {
                updateFocusState { it.copy(engineTypeBlurTimeout = intent.timeout) }
            }
        }
    }

    private fun loadCar(carId: String) {
        viewModelScope.launch {
            _state.value = CarEditMviState.Loading
            carBrandViewModel.loadBrands()
            bodyTypeViewModel.loadBodyTypes()
            driveTypeViewModel.loadDriveTypes()
            engineTypeViewModel.loadEngineTypes()
            runCatching {
                val car = carService.getCar(carId)
                val formData = mapToFormData(car)

                val resolvedFormData =
                    resolveBrandAndModelIds(formData, carBrandViewModel.brands.value, carModelViewModel.models.value)

                _state.value =
                    CarEditMviState.Success(
                        carId = carId,
                        formData = resolvedFormData,
                        hasChanges = false,
                    )
                _hasChanges.value = false
                resolvedFormData.brandId?.let { brandId ->
                    carModelViewModel.loadModels(brandId)
                }
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось загрузить данные автомобиля",
                    )
                _state.value = CarEditMviState.Error(errorMessage)
            }
        }
    }

    private fun saveCar() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState !is CarEditMviState.Success) return@launch
            val formData = currentState.formData
            val carIdToSave = currentState.carId

            _state.update { (it as? CarEditMviState.Success)?.copy(hasChanges = false) ?: it }
            _hasChanges.value = false
            runCatching {
                val engineVolumeValue = formData.engineVolume.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                val seatsCountValue =
                    formData.seatsCount
                        .takeIf { it.isNotBlank() }
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 }
                val hourlyRateValue = formData.hourlyRate.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                val dailyRateValue = formData.dailyRate.takeIf { it.isNotBlank() }?.toDoubleOrNull()
                val request =
                    CarCreateRequest(
                        id = formData.carId,
                        // brandId = formData.brandId,
                        modelId = formData.modelId,
                        bodyType = formData.bodyType,
                        driveType = formData.driveType,
                        engineType = formData.engineType,
                        engineVolume = engineVolumeValue,
                        // productionYear = formData.productionYear.toIntOrNull()?.takeIf { it > 0 },
                        seatsCount = seatsCountValue,
                        licensePlate = formData.licensePlate.takeIf { it.isNotBlank() },
                        ValidAddressString = formData.address,
                        hourlyRate = hourlyRateValue,
                        dailyRate = dailyRateValue,
                        ParkingAssistances = emptyList(),
                        MultimediaSystemOptions = emptyList(),
                    )
                carService.createOrUpdateCar(request, carIdToSave)
                myCarRepository.refresh()
                _state.update { (it as? CarEditMviState.Success)?.copy(hasChanges = false) ?: it }
                _hasChanges.value = false
                loadCar(carIdToSave)
                _state.value = CarEditMviState.NavigateToMyCars
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось сохранить изменения",
                    )
                _state.update {
                    (it as? CarEditMviState.Success)?.copy(
                        saveError = errorMessage,
                    ) ?: it
                }
            }
        }
    }

    private fun deleteCar() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState !is CarEditMviState.Success) return@launch
            val carIdToDelete = currentState.carId

            runCatching {
                carService.deleteCar(carIdToDelete)
                myCarRepository.refresh()
                _state.value = CarEditMviState.NavigateToMyCars
            }.onFailure { e ->
                val errorMessage =
                    ErrorHandler.extractErrorMessage(
                        exception = e,
                        defaultNetworkError = "Ошибка сети",
                        defaultGenericError = "Не удалось сохранить изменения",
                    )
                _state.update {
                    (it as? CarEditMviState.Success)?.copy(
                        saveError = errorMessage,
                    ) ?: it
                }
            }
        }
    }

    private fun selectBrand(
        brandId: Int,
        brandName: String,
    ) {
        updateFormData {
            it.copy(
                brandId = brandId,
                brandName = brandName,
                brandSearch = brandName,
                modelId = null,
                modelName = "",
                modelSearch = "",
            )
        }
        updateFocusState { it.copy(isBrandFocused = false) }
        carModelViewModel.loadModels(brandId)
    }

    private fun selectModel(
        modelId: Int,
        modelName: String,
    ) {
        updateFormData {
            it.copy(
                modelId = modelId,
                modelName = modelName,
                modelSearch = modelName,
            )
        }
        updateFocusState { it.copy(isModelFocused = false) }
    }

    private fun selectBodyType(
        bodyType: String,
        translate: String,
    ) {
        updateFormData {
            it.copy(
                bodyType = bodyType,
                bodyTypeTranslate = translate,
                bodyTypeSearch = translate,
            )
        }
        updateFocusState { it.copy(isBodyTypeFocused = false) }
    }

    private fun selectDriveType(
        driveType: String,
        translate: String,
    ) {
        updateFormData {
            it.copy(
                driveType = driveType,
                driveTypeTranslate = translate,
                driveTypeSearch = translate,
            )
        }
        updateFocusState { it.copy(isDriveTypeFocused = false) }
    }

    private fun selectEngineType(
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
        updateFocusState { it.copy(isEngineTypeFocused = false) }
    }

    private fun updateFormData(update: (CarEditFormData) -> CarEditFormData) {
        _state.update { currentState ->
            when (currentState) {
                is CarEditMviState.Success -> {
                    val updatedFormData = update(currentState.formData)
                    val newState =
                        currentState.copy(
                            formData = updatedFormData,
                            hasChanges = true,
                        )
                    _hasChanges.value = true
                    newState
                }
                else -> currentState
            }
        }
    }

    private fun updateFocusState(update: (CarEditMviState.Success) -> CarEditMviState.Success) {
        _state.update { currentState ->
            when (currentState) {
                is CarEditMviState.Success -> update(currentState)
                else -> currentState
            }
        }
    }

    private fun mapToFormData(car: CarDetailResponse): CarEditFormData =
        CarEditFormData(
            carId = car.id,
            licensePlate = car.resolvedLicensePlate() ?: "",
            brandId = car.resolvedBrandId(),
            brandName = car.resolvedBrandName() ?: "",
            brandSearch = car.resolvedBrandName() ?: "",
            modelId = car.resolvedModelId(),
            modelName = car.resolvedModelName() ?: "",
            modelSearch = car.resolvedModelName() ?: "",
            bodyType = car.resolvedBodyType(),
            bodyTypeTranslate = car.resolvedBodyTypeTranslate() ?: "",
            bodyTypeSearch = car.resolvedBodyTypeTranslate() ?: "",
            driveType = car.resolvedDriveType(),
            driveTypeTranslate = car.resolvedDriveTypeTranslate() ?: "",
            driveTypeSearch = car.resolvedDriveTypeTranslate() ?: "",
            engineType = car.resolvedEngineType(),
            engineTypeTranslate = car.resolvedEngineTypeTranslate() ?: "",
            engineTypeSearch = car.resolvedEngineTypeTranslate() ?: "",
            engineVolume = NumberFormatter.formatDouble(car.resolvedEngineVolume()),
            productionYear = NumberFormatter.formatInt(car.resolvedProductionYear()),
            seatsCount = NumberFormatter.formatInt(car.resolvedSeatsCount()),
            address = car.ValidAddressString ?: "",
            hourlyRate = NumberFormatter.formatDouble(car.resolvedHourlyRate()),
            dailyRate = NumberFormatter.formatDouble(car.resolvedDailyRate()),
            photos = car.photos.ifEmpty { car.general?.photos ?: emptyList() },
        )
}
