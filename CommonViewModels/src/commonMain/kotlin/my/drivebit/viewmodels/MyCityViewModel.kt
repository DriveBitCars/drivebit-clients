package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.repositories.MyCityRepository

interface MyCityViewModel {
    val myCity: Flow<String>

    fun refresh()
}

class MyCityViewModelImpl(
    private val myCityRepository: MyCityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MyCityViewModel {
    private val _myCity = MutableStateFlow<String>("")

    override val myCity: StateFlow<String> = _myCity.asStateFlow()

    init {
        loadCity()
    }

    private fun loadCity() {
        coroutineScope.launch {
            val city = myCityRepository.getSelectedCity()
            _myCity.value = city.name
        }
    }

    override fun refresh() {
        loadCity()
    }
}
