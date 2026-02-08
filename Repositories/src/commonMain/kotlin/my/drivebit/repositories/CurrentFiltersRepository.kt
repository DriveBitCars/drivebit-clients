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
}

internal class CurrentFiltersRepositoryImpl(
    private val settings: Settings,
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
    }

    private val currentTaskShortNameState = MutableStateFlow<String?>(null)
    private val startStateFlow = MutableStateFlow<String?>(null)
    private val endStateFlow = MutableStateFlow<String?>(null)
    private val dailyRateMinState = MutableStateFlow<Double?>(null)
    private val dailyRateMaxState = MutableStateFlow<Double?>(null)
    private val brandIdState = MutableStateFlow<Int?>(null)
    private val brandNameState = MutableStateFlow<String?>(null)
    private val modelIdState = MutableStateFlow<Int?>(null)
    private val modelNameState = MutableStateFlow<String?>(null)

    init {
        val savedTaskShortName = settings.getStringOrNullIfEmpty(CURRENT_TASK_SHORT_NAME_KEY)
        currentTaskShortNameState.value = savedTaskShortName

        val savedStartDate = settings.getStringOrNullIfEmpty(START_DATE_KEY)
        startStateFlow.value = savedStartDate

        val savedEndDate = settings.getStringOrNullIfEmpty(END_DATE_KEY)
        endStateFlow.value = savedEndDate

        val savedDailyRateMin = settings.getDouble(DAILY_RATE_MIN_KEY, -1.0)
        dailyRateMinState.value = if (savedDailyRateMin < 0) null else savedDailyRateMin

        val savedDailyRateMax = settings.getDouble(DAILY_RATE_MAX_KEY, -1.0)
        dailyRateMaxState.value = if (savedDailyRateMax < 0) null else savedDailyRateMax

        val savedBrandId = settings.getInt(BRAND_ID_KEY, -1)
        brandIdState.value = if (savedBrandId < 0) null else savedBrandId

        val savedBrandName = settings.getStringOrNullIfEmpty(BRAND_NAME_KEY)
        brandNameState.value = savedBrandName

        val savedModelId = settings.getInt(MODEL_ID_KEY, -1)
        modelIdState.value = if (savedModelId < 0) null else savedModelId

        val savedModelName = settings.getStringOrNullIfEmpty(MODEL_NAME_KEY)
        modelNameState.value = savedModelName
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

    override fun updateCurrentTask(shortName: String) {
        settings.putString(CURRENT_TASK_SHORT_NAME_KEY, shortName)
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
            settings.putDouble(DAILY_RATE_MIN_KEY, value)
        } else {
            settings.remove(DAILY_RATE_MIN_KEY)
        }
        dailyRateMinState.value = value
    }

    override fun updateDailyRateMax(value: Double?) {
        if (value != null) {
            settings.putDouble(DAILY_RATE_MAX_KEY, value)
        } else {
            settings.remove(DAILY_RATE_MAX_KEY)
        }
        dailyRateMaxState.value = value
    }

    override fun updateBrand(
        id: Int?,
        name: String?,
    ) {
        if (id != null) {
            settings.putInt(BRAND_ID_KEY, id)
        } else {
            settings.remove(BRAND_ID_KEY)
        }
        if (name != null) {
            settings.putString(BRAND_NAME_KEY, name)
        } else {
            settings.remove(BRAND_NAME_KEY)
        }
        brandIdState.value = id
        brandNameState.value = name
    }

    override fun updateModel(
        id: Int?,
        name: String?,
    ) {
        if (id != null) {
            settings.putInt(MODEL_ID_KEY, id)
        } else {
            settings.remove(MODEL_ID_KEY)
        }
        if (name != null) {
            settings.putString(MODEL_NAME_KEY, name)
        } else {
            settings.remove(MODEL_NAME_KEY)
        }
        modelIdState.value = id
        modelNameState.value = name
    }
}
