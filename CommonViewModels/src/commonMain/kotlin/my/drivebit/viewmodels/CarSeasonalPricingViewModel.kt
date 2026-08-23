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
import my.drivebit.network.services.SeasonalPriceAdjustmentDto
import my.drivebit.network.services.UpdateCarRequest
import my.drivebit.utils.isoDateTimeToLocalDate
import my.drivebit.utils.localDateToIsoDateTime
import my.drivebit.utils.parseSeasonalPercentInput
import my.drivebit.utils.validateSeasonalPriceAdjustmentPeriod

data class SeasonalPriceAdjustmentFormPeriod(
    val key: String,
    val id: String?,
    val startsAt: String,
    val endsAt: String,
    val percent: String,
)

sealed interface CarSeasonalPricingState {
    data object Loading : CarSeasonalPricingState

    data class Error(
        val message: String,
    ) : CarSeasonalPricingState

    data class Ready(
        val periods: List<SeasonalPriceAdjustmentFormPeriod>,
        val isSaving: Boolean = false,
        val formError: String? = null,
        val saved: Boolean = false,
    ) : CarSeasonalPricingState
}

interface CarSeasonalPricingViewModel {
    val state: StateFlow<CarSeasonalPricingState>

    fun addPeriod()

    fun removePeriod(key: String)

    fun updatePeriod(
        key: String,
        startsAt: String? = null,
        endsAt: String? = null,
        percent: String? = null,
    )

    fun save()
}

class CarSeasonalPricingViewModelImpl(
    private val carId: String,
    private val carService: Car,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CarSeasonalPricingViewModel {
    private val _state = MutableStateFlow<CarSeasonalPricingState>(CarSeasonalPricingState.Loading)
    private var nextKey = 1

    override val state: StateFlow<CarSeasonalPricingState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        if (carId.isBlank()) {
            _state.value = CarSeasonalPricingState.Error("Не указан ID автомобиля")
            return
        }
        coroutineScope.launch {
            _state.value = CarSeasonalPricingState.Loading
            runCatching { carService.getCar(carId) }
                .onSuccess { car ->
                    _state.value =
                        CarSeasonalPricingState.Ready(
                            periods = car.seasonalPriceAdjustments.map { it.toFormPeriod() },
                        )
                }.onFailure { e ->
                    _state.value =
                        CarSeasonalPricingState.Error(
                            ErrorHandler.extractErrorMessage(
                                exception = e,
                                defaultNetworkError = "Ошибка сети",
                                defaultGenericError = "Не удалось загрузить сезонные наценки",
                            ),
                        )
                }
        }
    }

    override fun addPeriod() {
        _state.update { current ->
            if (current !is CarSeasonalPricingState.Ready) return@update current
            current.copy(
                periods =
                    current.periods +
                        SeasonalPriceAdjustmentFormPeriod(
                            key = nextKey(),
                            id = null,
                            startsAt = "",
                            endsAt = "",
                            percent = "0",
                        ),
                formError = null,
                saved = false,
            )
        }
    }

    override fun removePeriod(key: String) {
        _state.update { current ->
            if (current !is CarSeasonalPricingState.Ready) return@update current
            current.copy(
                periods = current.periods.filter { it.key != key },
                formError = null,
                saved = false,
            )
        }
    }

    override fun updatePeriod(
        key: String,
        startsAt: String?,
        endsAt: String?,
        percent: String?,
    ) {
        _state.update { current ->
            if (current !is CarSeasonalPricingState.Ready) return@update current
            current.copy(
                periods =
                    current.periods.map { period ->
                        if (period.key != key) {
                            period
                        } else {
                            period.copy(
                                startsAt = startsAt ?: period.startsAt,
                                endsAt = endsAt ?: period.endsAt,
                                percent = percent ?: period.percent,
                            )
                        }
                    },
                formError = null,
                saved = false,
            )
        }
    }

    override fun save() {
        val current = _state.value
        if (current !is CarSeasonalPricingState.Ready) return

        val validationError =
            current.periods.firstNotNullOfOrNull { period ->
                validateSeasonalPriceAdjustmentPeriod(
                    startsAt = period.startsAt,
                    endsAt = period.endsAt,
                    percent = period.percent,
                )
            }
        if (validationError != null) {
            _state.value = current.copy(formError = validationError, saved = false)
            return
        }

        coroutineScope.launch {
            _state.update {
                if (it is CarSeasonalPricingState.Ready) it.copy(isSaving = true, formError = null) else it
            }
            runCatching {
                carService.updateCar(
                    UpdateCarRequest(
                        carId = carId,
                        seasonalPriceAdjustments = current.periods.map { it.toDto() },
                    ),
                )
            }.onSuccess {
                _state.update {
                    if (it is CarSeasonalPricingState.Ready) {
                        it.copy(isSaving = false, saved = true, formError = null)
                    } else {
                        it
                    }
                }
            }.onFailure { e ->
                _state.update {
                    if (it is CarSeasonalPricingState.Ready) {
                        it.copy(
                            isSaving = false,
                            saved = false,
                            formError =
                                ErrorHandler.extractErrorMessage(
                                    exception = e,
                                    defaultNetworkError = "Ошибка сети",
                                    defaultGenericError = "Не удалось сохранить сезонные наценки",
                                ),
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun nextKey(): String {
        val key = "new-$nextKey"
        nextKey += 1
        return key
    }

    private fun SeasonalPriceAdjustmentDto.toFormPeriod(): SeasonalPriceAdjustmentFormPeriod =
        SeasonalPriceAdjustmentFormPeriod(
            key = id ?: nextKey(),
            id = id,
            startsAt = isoDateTimeToLocalDate(startsAt),
            endsAt = isoDateTimeToLocalDate(endsAt),
            percent = formatSeasonalPercent(percent),
        )
}

private fun formatSeasonalPercent(value: Double): String {
    val asInt = value.toInt()
    return if (value == asInt.toDouble()) asInt.toString() else value.toString()
}

private fun SeasonalPriceAdjustmentFormPeriod.toDto(): SeasonalPriceAdjustmentDto =
    SeasonalPriceAdjustmentDto(
        id = id,
        startsAt = localDateToIsoDateTime(startsAt),
        endsAt = localDateToIsoDateTime(endsAt),
        percent = parseSeasonalPercentInput(percent)?.toDouble() ?: 0.0,
    )
