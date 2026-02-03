package my.drivebit.repositories

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CurrentTaskRepository {
    val currentTaskShortName: Flow<String?>

    fun updateCurrentTask(shortName: String)
}

internal class CurrentTaskRepositoryImpl(
    private val settings: Settings,
) : CurrentTaskRepository {
    companion object {
        private const val CURRENT_TASK_SHORT_NAME_KEY = "current_task_short_name"
    }

    private val currentTaskShortNameState = MutableStateFlow<String?>(null)

    init {
        val savedValue = settings.getStringOrNullIfEmpty(CURRENT_TASK_SHORT_NAME_KEY)
        currentTaskShortNameState.value = savedValue
    }

    override val currentTaskShortName: Flow<String?> = currentTaskShortNameState.asStateFlow()

    override fun updateCurrentTask(shortName: String) {
        settings.putString(CURRENT_TASK_SHORT_NAME_KEY, shortName)
        currentTaskShortNameState.value = shortName
    }
}
