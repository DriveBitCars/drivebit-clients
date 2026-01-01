package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import my.drivebit.network.services.AddressSuggestion
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import my.drivebit.repositories.SelectedCityRepository

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AddressSuggestViewModel(
    private val addressSuggestRepository: AddressSuggestRepository,
    private val selectedCityRepository: SelectedCityRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val viewModelScope = coroutineScope

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _suggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AddressSuggestion>> = _suggestions.asStateFlow()

    init {
        _query
            .debounce(500)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    val city = selectedCityRepository.getCityName()
                    if (city != null && query.isNotBlank() && query.length >= 3) {
                        when (val result = addressSuggestRepository.suggest(query, city)) {
                            is ResultAddressSuggest.Success -> {
                                emit(result.suggestions)
                            }
                            is ResultAddressSuggest.Error -> {
                                emit(emptyList())
                            }
                        }
                    } else {
                        emit(emptyList())
                    }
                }
            }.onEach { suggestions ->
                _suggestions.value = suggestions
            }.launchIn(viewModelScope)
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
}
