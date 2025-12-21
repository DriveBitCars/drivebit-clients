package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.drivebit.repositories.CarEnumsRepository
import my.drivebit.repositories.EnumItem

abstract class BaseEnumViewModel(
    protected val carEnumsRepository: CarEnumsRepository,
    protected val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val errorMessage: String,
) {
    protected val _allItems = MutableStateFlow<List<EnumItem>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _items = MutableStateFlow<List<EnumItem>>(emptyList())
    val items: StateFlow<List<EnumItem>> = _items.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    abstract suspend fun loadItems(): List<EnumItem>

    fun load() {
        coroutineScope.launch {
            _error.value = null
            runCatching {
                val allTypes = loadItems()
                _allItems.value = allTypes
                filterItems(_query.value)
            }.onFailure { e ->
                _error.value = e.message ?: errorMessage
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        filterItems(newQuery)
    }

    private fun filterItems(query: String) {
        if (query.isBlank()) {
            _items.value = _allItems.value
        } else {
            val lowerQuery = query.lowercase()
            _items.value =
                _allItems.value.filter {
                    it.translate.lowercase().contains(lowerQuery) ||
                        it.name.lowercase().contains(lowerQuery)
                }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _items.value = _allItems.value
    }
}
