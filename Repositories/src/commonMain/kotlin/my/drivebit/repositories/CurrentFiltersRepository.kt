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

    fun updateCurrentTask(shortName: String)

    fun updateStartDate(date: String?)

    fun updateEndDate(date: String?)

    fun updateDailyRateMin(value: Double?)

    fun updateDailyRateMax(value: Double?)
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
    }

    private val currentTaskShortNameState = MutableStateFlow<String?>(null)
    private val startStateFlow = MutableStateFlow<String?>(null)
    private val endStateFlow = MutableStateFlow<String?>(null)
    private val dailyRateMinState = MutableStateFlow<Double?>(null)
    private val dailyRateMaxState = MutableStateFlow<Double?>(null)

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
    }

    override val currentTaskShortName: Flow<String?> = currentTaskShortNameState.asStateFlow()
    override val startState: Flow<String?> = startStateFlow.asStateFlow()
    override val endState: Flow<String?> = endStateFlow.asStateFlow()
    override val dailyRateMin: Flow<Double?> = dailyRateMinState.asStateFlow()
    override val dailyRateMax: Flow<Double?> = dailyRateMaxState.asStateFlow()

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
}
