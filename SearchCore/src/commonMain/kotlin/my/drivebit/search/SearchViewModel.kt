package my.drivebit.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.drivebit.utils.SearchUrlParts
import my.drivebit.utils.parseSearchUrl

sealed interface SearchUiState {
    data object Loading : SearchUiState

    data class Results(
        val filters: SearchFilterSet,
        val result: SearchCarsResult,
    ) : SearchUiState

    data class Error(
        val message: String,
    ) : SearchUiState
}

class SearchViewModel(
    private val repositoryFactory: (SearchFilterSet) -> SearchCarRepository,
    private val brandResolver: suspend (String) -> Pair<Int, String>?,
    private val modelResolver: suspend (brandId: Int, modelSlug: String) -> Pair<Int, String>?,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun load(url: String) {
        coroutineScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val parts = parseSearchUrl(url)
                val filters = resolveFilters(parts)
                val result = repositoryFactory(filters).results.first()
                _state.value = SearchUiState.Results(filters = filters, result = result)
            } catch (e: Exception) {
                _state.value = SearchUiState.Error(e.message ?: "Не удалось выполнить поиск")
            }
        }
    }

    private suspend fun resolveFilters(parts: SearchUrlParts): SearchFilterSet {
        var brandId: Int? = null
        var brandName: String? = null
        var modelId: Int? = null
        var modelName: String? = null
        val brandSlug = parts.brandSlug
        if (brandSlug != null) {
            val brand = brandResolver(brandSlug)
            brandId = brand?.first
            brandName = brand?.second
            val modelSlug = parts.modelSlug
            if (brandId != null && modelSlug != null) {
                val model = modelResolver(brandId, modelSlug)
                modelId = model?.first
                modelName = model?.second
            }
        }
        return SearchFilterSet(
            citySlug = parts.citySlug,
            brandSlug = parts.brandSlug,
            modelSlug = parts.modelSlug,
            brandId = brandId,
            brandName = brandName,
            modelId = modelId,
            modelName = modelName,
            startDate = parts.startDate,
            endDate = parts.endDate,
            dailyRateMin = parts.dailyRateMin,
            dailyRateMax = parts.dailyRateMax,
            driveType = parts.driveType,
            driveTypeLabel = parts.driveTypeLabel,
            bodyType = parts.bodyType,
            bodyTypeLabel = parts.bodyTypeLabel,
            seatsMin = parts.seatsMin,
            yearMin = parts.yearMin,
            yearMax = parts.yearMax,
            mileageMin = parts.mileageMin,
            page = parts.page,
        )
    }
}
