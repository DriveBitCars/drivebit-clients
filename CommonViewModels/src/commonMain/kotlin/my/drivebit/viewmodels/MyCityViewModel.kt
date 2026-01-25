package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import my.drivebit.repositories.MyCityRepository

interface MyCityViewModel {
    val myCity: Flow<String>

    fun refresh()
}

class MyCityViewModelImpl(
    private val myCityRepository: MyCityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyCityViewModel {
    override val myCity: Flow<String> =
        flow {
            emitAll(myCityRepository.getSelectedCity)
        }.map { it.name }

    override fun refresh() {
    }
}
