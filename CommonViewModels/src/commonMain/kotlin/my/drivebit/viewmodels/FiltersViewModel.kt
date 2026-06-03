package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.repositories.SuggestedFiltersCatalog
import my.drivebit.resources.ImagePaths.FILTER_MAIN_CAR_SVG
import my.drivebit.resources.ImagePaths.FILTER_MAIN_POINT_SVG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR0_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR1_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR2_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR3_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR4_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR5_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR6_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR7_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR8_JPG

data class FilterItem(
    val icon: String,
    val title: String,
    val backgroundIcon: String,
)

data class FilterScreenState(
    val selected: String,
    val filters: List<FilterItem>,
)

class FiltersViewModel(
    private val currentFiltersRepository: CurrentFiltersRepository,
) {
    private val backgroundIcons =
        listOf(
            SEARCHBACKGROUND_CAR2_JPG,
            SEARCHBACKGROUND_CAR3_JPG,
            SEARCHBACKGROUND_CAR4_JPG,
            SEARCHBACKGROUND_CAR5_JPG,
            SEARCHBACKGROUND_CAR6_JPG,
            SEARCHBACKGROUND_CAR7_JPG,
            SEARCHBACKGROUND_CAR8_JPG,
        )

    private val _state =
        MutableStateFlow(
            FilterScreenState(
                selected = getSelectedFilter(),
                filters = emptyList(),
            ),
        )

    val state
        get() = _state.asStateFlow()

    init {
        _state.update { it.copy(filters = buildFilterItems()) }
    }

    private fun buildFilterItems(): List<FilterItem> {
        val filterItems = mutableListOf<FilterItem>()

        filterItems.add(
            FilterItem(
                icon = FILTER_MAIN_CAR_SVG,
                title = "Все",
                backgroundIcon = SEARCHBACKGROUND_CAR0_JPG,
            ),
        )

        filterItems.add(
            FilterItem(
                icon = FILTER_MAIN_POINT_SVG,
                title = "Поблизости",
                backgroundIcon = SEARCHBACKGROUND_CAR1_JPG,
            ),
        )

        SuggestedFiltersCatalog.suggested.forEachIndexed { index, suggestion ->
            val backgroundIcon =
                backgroundIcons.getOrNull(index % backgroundIcons.size)
                    ?: SEARCHBACKGROUND_CAR2_JPG

            filterItems.add(
                FilterItem(
                    icon = suggestion.iconUrl,
                    title = suggestion.shortName,
                    backgroundIcon = backgroundIcon,
                ),
            )
        }

        return filterItems
    }

    fun onSelect(title: String) {
        val effectiveTitle =
            if (title != "Все" && title == _state.value.selected) "Все" else title
        _state.update { it.copy(selected = effectiveTitle) }
        currentFiltersRepository.updateCurrentTask(effectiveTitle)
        if (effectiveTitle == "Все") {
            clearAllFilters()
        }
    }

    private fun clearAllFilters() {
        currentFiltersRepository.updateDailyRateMin(null)
        currentFiltersRepository.updateDailyRateMax(null)
        currentFiltersRepository.updateBrand(null, null)
        currentFiltersRepository.updateModel(null, null)
        currentFiltersRepository.updateDriveType(null, null)
        currentFiltersRepository.updateBodyType(null, null)
        currentFiltersRepository.updateSeatsMin(null)
        currentFiltersRepository.updateEngineType(null, null)
        currentFiltersRepository.updateColor(null, null)
        currentFiltersRepository.updateYearMin(null)
        currentFiltersRepository.updateYearMax(null)
        currentFiltersRepository.updateSeatsMax(null)
        currentFiltersRepository.updateAvailableMileagePerDayKmMin(null)
    }

    private fun getSelectedFilter(): String = "Все"
}
