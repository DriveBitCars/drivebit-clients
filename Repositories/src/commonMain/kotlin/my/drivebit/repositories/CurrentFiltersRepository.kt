package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CurrentFiltersRepository {
    val currentTaskShortName: Flow<String?>
    val startState: Flow<String?>
    val endState: Flow<String?>
    val dailyRateMin: Flow<Double?>
    val dailyRateMax: Flow<Double?>
    val brandId: Flow<Int?>
    val brandName: Flow<String?>
    val modelId: Flow<Int?>
    val modelName: Flow<String?>
    val driveTypeName: Flow<String?>
    val driveTypeTranslate: Flow<String?>
    val bodyTypeName: Flow<String?>
    val bodyTypeTranslate: Flow<String?>
    val seatsMin: Flow<Int?>
    val engineTypeName: Flow<String?>
    val engineTypeTranslate: Flow<String?>
    val colorName: Flow<String?>
    val colorTranslate: Flow<String?>
    val yearMin: Flow<Int?>
    val yearMax: Flow<Int?>
    val seatsMax: Flow<Int?>
    val availableMileagePerDayKmMin: Flow<Int?>

    fun updateCurrentTask(shortName: String)

    fun updateStartDate(date: String?)

    fun updateEndDate(date: String?)

    fun updateDailyRateMin(value: Double?)

    fun updateDailyRateMax(value: Double?)

    fun updateBrand(
        id: Int?,
        name: String?,
    )

    fun updateModel(
        id: Int?,
        name: String?,
    )

    fun updateDriveType(
        name: String?,
        translate: String?,
    )

    fun updateBodyType(
        name: String?,
        translate: String?,
    )

    fun updateSeatsMin(value: Int?)

    fun updateEngineType(
        name: String?,
        translate: String?,
    )

    fun updateColor(
        name: String?,
        translate: String?,
    )

    fun updateYearMin(value: Int?)

    fun updateYearMax(value: Int?)

    fun updateSeatsMax(value: Int?)

    fun updateAvailableMileagePerDayKmMin(value: Int?)
}

internal class CurrentFiltersRepositoryImpl(
    private val settings: Settings,
    private val keyPrefix: String? = null,
) : CurrentFiltersRepository {
    companion object {
        private const val CURRENT_TASK_SHORT_NAME_KEY = "current_task_short_name"
        private const val START_DATE_KEY = "start_date"
        private const val END_DATE_KEY = "end_date"
        private const val DAILY_RATE_MIN_KEY = "daily_rate_min"
        private const val DAILY_RATE_MAX_KEY = "daily_rate_max"
        private const val BRAND_ID_KEY = "filter_brand_id"
        private const val BRAND_NAME_KEY = "filter_brand_name"
        private const val MODEL_ID_KEY = "filter_model_id"
        private const val MODEL_NAME_KEY = "filter_model_name"
        private const val DRIVE_TYPE_NAME_KEY = "filter_drive_type_name"
        private const val DRIVE_TYPE_TRANSLATE_KEY = "filter_drive_type_translate"
        private const val BODY_TYPE_NAME_KEY = "filter_body_type_name"
        private const val BODY_TYPE_TRANSLATE_KEY = "filter_body_type_translate"
        private const val SEATS_MIN_KEY = "filter_seats_min"
        private const val ENGINE_TYPE_NAME_KEY = "filter_engine_type_name"
        private const val ENGINE_TYPE_TRANSLATE_KEY = "filter_engine_type_translate"
        private const val COLOR_NAME_KEY = "filter_color_name"
        private const val COLOR_TRANSLATE_KEY = "filter_color_translate"
        private const val YEAR_MIN_KEY = "filter_year_min"
        private const val YEAR_MAX_KEY = "filter_year_max"
        private const val SEATS_MAX_KEY = "filter_seats_max"
        private const val AVAILABLE_MILEAGE_PER_DAY_KM_MIN_KEY = "filter_available_mileage_per_day_km_min"
    }

    private fun key(name: String): String = if (keyPrefix != null) "${keyPrefix}_$name" else name

    private val currentTaskShortNameState = MutableStateFlow<String?>(null)
    private val startStateFlow = MutableStateFlow<String?>(null)
    private val endStateFlow = MutableStateFlow<String?>(null)
    private val dailyRateMinState = MutableStateFlow<Double?>(null)
    private val dailyRateMaxState = MutableStateFlow<Double?>(null)
    private val brandIdState = MutableStateFlow<Int?>(null)
    private val brandNameState = MutableStateFlow<String?>(null)
    private val modelIdState = MutableStateFlow<Int?>(null)
    private val modelNameState = MutableStateFlow<String?>(null)
    private val driveTypeNameState = MutableStateFlow<String?>(null)
    private val driveTypeTranslateState = MutableStateFlow<String?>(null)
    private val bodyTypeNameState = MutableStateFlow<String?>(null)
    private val bodyTypeTranslateState = MutableStateFlow<String?>(null)
    private val seatsMinState = MutableStateFlow<Int?>(null)
    private val engineTypeNameState = MutableStateFlow<String?>(null)
    private val engineTypeTranslateState = MutableStateFlow<String?>(null)
    private val colorNameState = MutableStateFlow<String?>(null)
    private val colorTranslateState = MutableStateFlow<String?>(null)
    private val yearMinState = MutableStateFlow<Int?>(null)
    private val yearMaxState = MutableStateFlow<Int?>(null)
    private val seatsMaxState = MutableStateFlow<Int?>(null)
    private val availableMileagePerDayKmMinState = MutableStateFlow<Int?>(null)

    init {
        val savedTaskShortName = settings.getStringOrNullIfEmpty(key(CURRENT_TASK_SHORT_NAME_KEY))
        currentTaskShortNameState.value = savedTaskShortName

        val savedStartDate = settings.getStringOrNullIfEmpty(START_DATE_KEY)
        startStateFlow.value = savedStartDate

        val savedEndDate = settings.getStringOrNullIfEmpty(END_DATE_KEY)
        endStateFlow.value = savedEndDate

        val savedDailyRateMin = settings.getDouble(key(DAILY_RATE_MIN_KEY), -1.0)
        dailyRateMinState.value = if (savedDailyRateMin < 0) null else savedDailyRateMin

        val savedDailyRateMax = settings.getDouble(key(DAILY_RATE_MAX_KEY), -1.0)
        dailyRateMaxState.value = if (savedDailyRateMax < 0) null else savedDailyRateMax

        val savedBrandId = settings.getInt(key(BRAND_ID_KEY), -1)
        brandIdState.value = if (savedBrandId < 0) null else savedBrandId

        val savedBrandName = settings.getStringOrNullIfEmpty(key(BRAND_NAME_KEY))
        brandNameState.value = savedBrandName

        val savedModelId = settings.getInt(key(MODEL_ID_KEY), -1)
        modelIdState.value = if (savedModelId < 0) null else savedModelId

        val savedModelName = settings.getStringOrNullIfEmpty(key(MODEL_NAME_KEY))
        modelNameState.value = savedModelName

        val savedDriveTypeName = settings.getStringOrNullIfEmpty(key(DRIVE_TYPE_NAME_KEY))
        driveTypeNameState.value = savedDriveTypeName

        val savedDriveTypeTranslate = settings.getStringOrNullIfEmpty(key(DRIVE_TYPE_TRANSLATE_KEY))
        driveTypeTranslateState.value = savedDriveTypeTranslate

        val savedBodyTypeName = settings.getStringOrNullIfEmpty(key(BODY_TYPE_NAME_KEY))
        bodyTypeNameState.value = savedBodyTypeName

        val savedBodyTypeTranslate = settings.getStringOrNullIfEmpty(key(BODY_TYPE_TRANSLATE_KEY))
        bodyTypeTranslateState.value = savedBodyTypeTranslate

        val savedSeatsMin = settings.getInt(key(SEATS_MIN_KEY), -1)
        seatsMinState.value = if (savedSeatsMin < 0) null else savedSeatsMin

        val savedEngineTypeName = settings.getStringOrNullIfEmpty(key(ENGINE_TYPE_NAME_KEY))
        engineTypeNameState.value = savedEngineTypeName

        val savedEngineTypeTranslate = settings.getStringOrNullIfEmpty(key(ENGINE_TYPE_TRANSLATE_KEY))
        engineTypeTranslateState.value = savedEngineTypeTranslate

        val savedColorName = settings.getStringOrNullIfEmpty(key(COLOR_NAME_KEY))
        colorNameState.value = savedColorName

        val savedColorTranslate = settings.getStringOrNullIfEmpty(key(COLOR_TRANSLATE_KEY))
        colorTranslateState.value = savedColorTranslate

        val savedYearMin = settings.getInt(key(YEAR_MIN_KEY), -1)
        yearMinState.value = if (savedYearMin < 0) null else savedYearMin

        val savedYearMax = settings.getInt(key(YEAR_MAX_KEY), -1)
        yearMaxState.value = if (savedYearMax < 0) null else savedYearMax

        val savedSeatsMax = settings.getInt(key(SEATS_MAX_KEY), -1)
        seatsMaxState.value = if (savedSeatsMax < 0) null else savedSeatsMax

        val savedAvailableMileage = settings.getInt(key(AVAILABLE_MILEAGE_PER_DAY_KM_MIN_KEY), -1)
        availableMileagePerDayKmMinState.value = if (savedAvailableMileage < 0) null else savedAvailableMileage
    }

    override val currentTaskShortName: Flow<String?> = currentTaskShortNameState.asStateFlow()
    override val startState: Flow<String?> = startStateFlow.asStateFlow()
    override val endState: Flow<String?> = endStateFlow.asStateFlow()
    override val dailyRateMin: Flow<Double?> = dailyRateMinState.asStateFlow()
    override val dailyRateMax: Flow<Double?> = dailyRateMaxState.asStateFlow()
    override val brandId: Flow<Int?> = brandIdState.asStateFlow()
    override val brandName: Flow<String?> = brandNameState.asStateFlow()
    override val modelId: Flow<Int?> = modelIdState.asStateFlow()
    override val modelName: Flow<String?> = modelNameState.asStateFlow()
    override val driveTypeName: Flow<String?> = driveTypeNameState.asStateFlow()
    override val driveTypeTranslate: Flow<String?> = driveTypeTranslateState.asStateFlow()
    override val bodyTypeName: Flow<String?> = bodyTypeNameState.asStateFlow()
    override val bodyTypeTranslate: Flow<String?> = bodyTypeTranslateState.asStateFlow()
    override val seatsMin: Flow<Int?> = seatsMinState.asStateFlow()
    override val engineTypeName: Flow<String?> = engineTypeNameState.asStateFlow()
    override val engineTypeTranslate: Flow<String?> = engineTypeTranslateState.asStateFlow()
    override val colorName: Flow<String?> = colorNameState.asStateFlow()
    override val colorTranslate: Flow<String?> = colorTranslateState.asStateFlow()
    override val yearMin: Flow<Int?> = yearMinState.asStateFlow()
    override val yearMax: Flow<Int?> = yearMaxState.asStateFlow()
    override val seatsMax: Flow<Int?> = seatsMaxState.asStateFlow()
    override val availableMileagePerDayKmMin: Flow<Int?> = availableMileagePerDayKmMinState.asStateFlow()

    override fun updateCurrentTask(shortName: String) {
        settings.putString(key(CURRENT_TASK_SHORT_NAME_KEY), shortName)
        currentTaskShortNameState.value = shortName
    }

    override fun updateStartDate(date: String?) {
        if (date != null) {
            settings.putString(START_DATE_KEY, date)
        } else {
            settings.remove(START_DATE_KEY)
        }
        startStateFlow.value = date
    }

    override fun updateEndDate(date: String?) {
        if (date != null) {
            settings.putString(END_DATE_KEY, date)
        } else {
            settings.remove(END_DATE_KEY)
        }
        endStateFlow.value = date
    }

    override fun updateDailyRateMin(value: Double?) {
        if (value != null) {
            settings.putDouble(key(DAILY_RATE_MIN_KEY), value)
        } else {
            settings.remove(key(DAILY_RATE_MIN_KEY))
        }
        dailyRateMinState.value = value
    }

    override fun updateDailyRateMax(value: Double?) {
        if (value != null) {
            settings.putDouble(key(DAILY_RATE_MAX_KEY), value)
        } else {
            settings.remove(key(DAILY_RATE_MAX_KEY))
        }
        dailyRateMaxState.value = value
    }

    override fun updateBrand(
        id: Int?,
        name: String?,
    ) {
        if (id != null) {
            settings.putInt(key(BRAND_ID_KEY), id)
        } else {
            settings.remove(key(BRAND_ID_KEY))
        }
        if (name != null) {
            settings.putString(key(BRAND_NAME_KEY), name)
        } else {
            settings.remove(key(BRAND_NAME_KEY))
        }
        brandIdState.value = id
        brandNameState.value = name
    }

    override fun updateModel(
        id: Int?,
        name: String?,
    ) {
        if (id != null) {
            settings.putInt(key(MODEL_ID_KEY), id)
        } else {
            settings.remove(key(MODEL_ID_KEY))
        }
        if (name != null) {
            settings.putString(key(MODEL_NAME_KEY), name)
        } else {
            settings.remove(key(MODEL_NAME_KEY))
        }
        modelIdState.value = id
        modelNameState.value = name
    }

    override fun updateDriveType(
        name: String?,
        translate: String?,
    ) {
        if (name != null) {
            settings.putString(key(DRIVE_TYPE_NAME_KEY), name)
        } else {
            settings.remove(key(DRIVE_TYPE_NAME_KEY))
        }
        if (translate != null) {
            settings.putString(key(DRIVE_TYPE_TRANSLATE_KEY), translate)
        } else {
            settings.remove(key(DRIVE_TYPE_TRANSLATE_KEY))
        }
        driveTypeNameState.value = name
        driveTypeTranslateState.value = translate
    }

    override fun updateBodyType(
        name: String?,
        translate: String?,
    ) {
        if (name != null) {
            settings.putString(key(BODY_TYPE_NAME_KEY), name)
        } else {
            settings.remove(key(BODY_TYPE_NAME_KEY))
        }
        if (translate != null) {
            settings.putString(key(BODY_TYPE_TRANSLATE_KEY), translate)
        } else {
            settings.remove(key(BODY_TYPE_TRANSLATE_KEY))
        }
        bodyTypeNameState.value = name
        bodyTypeTranslateState.value = translate
    }

    override fun updateSeatsMin(value: Int?) {
        if (value != null) {
            settings.putInt(key(SEATS_MIN_KEY), value)
        } else {
            settings.remove(key(SEATS_MIN_KEY))
        }
        seatsMinState.value = value
    }

    override fun updateEngineType(
        name: String?,
        translate: String?,
    ) {
        if (name != null) {
            settings.putString(key(ENGINE_TYPE_NAME_KEY), name)
        } else {
            settings.remove(key(ENGINE_TYPE_NAME_KEY))
        }
        if (translate != null) {
            settings.putString(key(ENGINE_TYPE_TRANSLATE_KEY), translate)
        } else {
            settings.remove(key(ENGINE_TYPE_TRANSLATE_KEY))
        }
        engineTypeNameState.value = name
        engineTypeTranslateState.value = translate
    }

    override fun updateColor(
        name: String?,
        translate: String?,
    ) {
        if (name != null) {
            settings.putString(key(COLOR_NAME_KEY), name)
        } else {
            settings.remove(key(COLOR_NAME_KEY))
        }
        if (translate != null) {
            settings.putString(key(COLOR_TRANSLATE_KEY), translate)
        } else {
            settings.remove(key(COLOR_TRANSLATE_KEY))
        }
        colorNameState.value = name
        colorTranslateState.value = translate
    }

    override fun updateYearMin(value: Int?) {
        if (value != null) {
            settings.putInt(key(YEAR_MIN_KEY), value)
        } else {
            settings.remove(key(YEAR_MIN_KEY))
        }
        yearMinState.value = value
    }

    override fun updateYearMax(value: Int?) {
        if (value != null) {
            settings.putInt(key(YEAR_MAX_KEY), value)
        } else {
            settings.remove(key(YEAR_MAX_KEY))
        }
        yearMaxState.value = value
    }

    override fun updateSeatsMax(value: Int?) {
        if (value != null) {
            settings.putInt(key(SEATS_MAX_KEY), value)
        } else {
            settings.remove(key(SEATS_MAX_KEY))
        }
        seatsMaxState.value = value
    }

    override fun updateAvailableMileagePerDayKmMin(value: Int?) {
        if (value != null) {
            settings.putInt(key(AVAILABLE_MILEAGE_PER_DAY_KM_MIN_KEY), value)
        } else {
            settings.remove(key(AVAILABLE_MILEAGE_PER_DAY_KM_MIN_KEY))
        }
        availableMileagePerDayKmMinState.value = value
    }
}
