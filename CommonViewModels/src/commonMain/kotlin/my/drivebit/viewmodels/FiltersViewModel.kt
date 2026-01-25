package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import my.drivebit.resources.ImagePaths.FILTER_MAIN_CAR_SVG
import my.drivebit.resources.ImagePaths.FILTER_MAIN_POINT_SVG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR0_JPG
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR1_JPG
import my.drivebit.shared.storage.Storage

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
    private val storage: Storage,
) {
    private val _state =
        MutableStateFlow(
            FilterScreenState(
                selected = getSelectedFilter(),
                filters =
                    listOf(
                        FilterItem(
                            icon = FILTER_MAIN_CAR_SVG,
                            title = "Все",
                            backgroundIcon = SEARCHBACKGROUND_CAR0_JPG,
                        ),
                        FilterItem(
                            icon = FILTER_MAIN_POINT_SVG,
                            title = "Поблизости",
                            backgroundIcon = SEARCHBACKGROUND_CAR1_JPG,
                        ),
                    ),
            ),
        )

    val state
        get() = _state.asStateFlow()

    var onNearbyFilterSelected: (() -> Unit)? = null

    fun onSelect(title: String) {
        _state.update { it.copy(selected = title) }
        saveSelectedFilter(title)

        if (title == "Поблизости") {
            onNearbyFilterSelected?.invoke()
        }
    }

    private fun getSelectedFilter(): String = "Все"

    private fun saveSelectedFilter(filter: String) {
    }
}
