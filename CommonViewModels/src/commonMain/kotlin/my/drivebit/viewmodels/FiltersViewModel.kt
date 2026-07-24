package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val filters: List<FilterItem>,
)

class FiltersViewModel {
    private val backgroundByFilterTitle =
        mapOf(
            "В Крым" to SEARCHBACKGROUND_CAR2_JPG,
            "Беларусь" to SEARCHBACKGROUND_CAR4_JPG,
            "Абхазия" to SEARCHBACKGROUND_CAR5_JPG,
            "Премиум" to SEARCHBACKGROUND_CAR3_JPG,
            "Эконом" to SEARCHBACKGROUND_CAR6_JPG,
            "Комфорт" to SEARCHBACKGROUND_CAR5_JPG,
            "Бизнес" to SEARCHBACKGROUND_CAR2_JPG,
            "Минивэн" to SEARCHBACKGROUND_CAR8_JPG,
            "Внедорожник" to SEARCHBACKGROUND_CAR7_JPG,
        )

    private val _state =
        MutableStateFlow(
            FilterScreenState(
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

        SuggestedFiltersCatalog.suggested.forEach { suggestion ->
            val backgroundIcon =
                backgroundByFilterTitle[suggestion.shortName] ?: SEARCHBACKGROUND_CAR2_JPG

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
}
