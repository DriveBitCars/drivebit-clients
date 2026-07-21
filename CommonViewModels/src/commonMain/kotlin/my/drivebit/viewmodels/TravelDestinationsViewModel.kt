package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.repositories.CarEnumsRepository
import my.drivebit.repositories.EnumItem
import my.drivebit.repositories.SelectedTravelDestinationsRepository

class TravelDestinationsViewModel(
    private val carEnumsRepository: CarEnumsRepository,
    private val selectedTravelDestinationsRepository: SelectedTravelDestinationsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _options = MutableStateFlow<List<EnumItem>>(emptyList())
    val options: StateFlow<List<EnumItem>> = _options.asStateFlow()

    private val _selectedNames = MutableStateFlow<List<String>>(emptyList())
    val selectedNames: StateFlow<List<String>> = _selectedNames.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load() {
        coroutineScope.launch {
            runCatching {
                carEnumsRepository.getAllTravelDestinations()
            }.onSuccess { destinations ->
                _options.value = destinations
                _error.value = null
                val saved = selectedTravelDestinationsRepository.getDestinationNames()
                if (saved.isNotEmpty()) {
                    _selectedNames.value = saved
                }
            }.onFailure {
                _error.value = "Не удалось загрузить направления путешествий"
            }
        }
    }

    fun toggle(name: String) {
        _selectedNames.update { current ->
            if (name in current) current - name else current + name
        }
    }

    fun confirmSelection() {
        val names = _selectedNames.value
        val translates =
            names.mapNotNull { name ->
                _options.value.find { it.name == name }?.translate
            }
        selectedTravelDestinationsRepository.saveDestinations(names, translates)
    }
}
