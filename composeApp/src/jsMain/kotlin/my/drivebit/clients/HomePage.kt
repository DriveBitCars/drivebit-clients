package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.shell.AppWithHeader
import my.drivebit.components.CarsGrid
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.HeroBanner
import my.drivebit.components.PaginationBar
import my.drivebit.components.filterButton
import my.drivebit.navigation.NavigationState
import my.drivebit.web.navigateToCarDetail
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.web.cityPathWithFilter
import my.drivebit.web.filterTitleToPathSegment
import my.drivebit.web.isCityHomePath
import my.drivebit.web.parseCitySlugFromPath
import my.drivebit.web.parseFilterSlugFromCityPath
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private const val NEARBY_FILTER_TITLE = "Поблизости"

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val mainContentViewModel: MainContentViewModel = koinInject()
    val currentFiltersRepository: CurrentFiltersRepository = koinInject(named("main"))
    val navigationState: NavigationState = koinInject()

    val state = filterViewModel.state.collectAsState()
    val currentPath by navigationState.currentPath.collectAsState()
    val displayedCars by mainContentViewModel.displayedCars.collectAsState()
    val paginationInfo by mainContentViewModel.paginationInfo.collectAsState()
    val filters = state.value.filters
    val selectedFromPath =
        parseFilterSlugFromCityPath(currentPath)?.let { filterSlug ->
            filters.firstOrNull { filterTitleToPathSegment(it.title) == filterSlug }?.title
        }
    val selected = selectedFromPath ?: state.value.selected

    val startDateViewModel: DateFieldViewModel = koinInject(named("startDate"))
    val endDateViewModel: DateFieldViewModel = koinInject(named("endDate"))
    val myCityViewModel: MyCityViewModel = koinInject()
    val cityName by myCityViewModel.myCity.collectAsState(initial = "")

    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()

    LaunchedEffect(currentPath) {
        if (parseFilterSlugFromCityPath(currentPath) != "poblizosti") return@LaunchedEffect
        val search = window.location.search
        val targetPath = currentPath.trimEnd('/').ifEmpty { "/" }
        val currentPathname = window.location.pathname.trimEnd('/').ifEmpty { "/" }
        if (currentPathname != targetPath) {
            window.location.href = currentPath + search
        }
    }

    LaunchedEffect(startState.date) {
        currentFiltersRepository.updateStartDate(startState.date)
    }

    LaunchedEffect(endState.date) {
        currentFiltersRepository.updateEndDate(endState.date)
    }

    LaunchedEffect(selectedFromPath) {
        if (selectedFromPath != null && selectedFromPath != state.value.selected) {
            filterViewModel.onSelect(selectedFromPath)
        }
    }

    LaunchedEffect(currentPath, cityName) {
        if (isCityHomePath(currentPath) && cityName.isNotEmpty()) {
            mainContentViewModel.refresh()
        }
    }

    AppWithHeader {
        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            HeroBanner(
                backgroundIconUrl = filter.backgroundIcon,
                cityName = cityName,
                citySlug = parseCitySlugFromPath(currentPath).orEmpty(),
                filterSlug = parseFilterSlugFromCityPath(currentPath),
                filterTitle = selected,
                startDateViewModel = startDateViewModel,
                endDateViewModel = endDateViewModel,
            )
        }

        FilterButtonsRow {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == selected,
                    onClick = {
                        parseCitySlugFromPath(currentPath)?.let { citySlug ->
                            val effectiveTitle =
                                if (filter.title != "Все" && filter.title == selected) "Все" else filter.title
                            val targetPath = cityPathWithFilter(citySlug, effectiveTitle)
                            if (targetPath != currentPath) {
                                if (effectiveTitle == NEARBY_FILTER_TITLE) {
                                    window.location.href = targetPath + window.location.search
                                } else {
                                    window.history.pushState(null, "", targetPath)
                                    navigationState.updatePath(targetPath)
                                    filterViewModel.onSelect(filter.title)
                                }
                                return@filterButton
                            }
                        }
                        filterViewModel.onSelect(filter.title)
                    },
                )
            }
        }

        Div({
            style {
                width(100.percent)
                marginTop(20.px)
            }
        }) {
            CarsGrid(
                cars = displayedCars,
                onCarClick = { car ->
                    navigateToCarDetail(
                        carId = car.id,
                        startDate = startState.date,
                        endDate = endState.date,
                    )
                },
            )
            PaginationBar(
                currentPage = paginationInfo.first,
                totalPages = paginationInfo.second,
                totalCount = paginationInfo.third,
                pageSize = 9,
                onPageChange = { mainContentViewModel.setPage(it) },
            )
        }
    }
}
