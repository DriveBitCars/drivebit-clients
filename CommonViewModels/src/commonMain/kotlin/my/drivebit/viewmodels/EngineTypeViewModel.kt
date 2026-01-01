package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import my.drivebit.repositories.CarEnumsRepository
import my.drivebit.repositories.EnumItem

class EngineTypeViewModel(
    carEnumsRepository: CarEnumsRepository,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BaseEnumViewModel(
        carEnumsRepository = carEnumsRepository,
        coroutineScope = coroutineScope,
        errorMessage = "Не удалось загрузить типы двигателя",
    ) {
    val engineTypes: StateFlow<List<EnumItem>> = items

    override suspend fun loadItems() = carEnumsRepository.getAllEngineTypes()

    fun loadEngineTypes() = load()
}
