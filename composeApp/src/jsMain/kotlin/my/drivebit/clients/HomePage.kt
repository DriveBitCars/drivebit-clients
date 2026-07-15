package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.components.CarsGrid
import my.drivebit.components.CarsGridSkeleton
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.NearbyMapListSwitcher
import my.drivebit.components.NearbyRadiusSelector
import my.drivebit.components.PaginationBar
import my.drivebit.components.TextError
import my.drivebit.components.filterButton
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.shell.AppWithHeader
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentListState
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.viewmodels.NearbyLayoutMode
import my.drivebit.web.HtmlFiltersBridge
import my.drivebit.web.HtmlHeroDatesBridge
import my.drivebit.web.cityPathWithFilter
import my.drivebit.web.filterTitleFromPathSegment
import my.drivebit.web.filterTitleToPathSegment
import my.drivebit.web.isCityHomePath
import my.drivebit.web.buildCarDetailUrl
import my.drivebit.web.navigateToCarDetail
import my.drivebit.web.parseCitySlugFromPath
import my.drivebit.web.parseFilterSlugFromCityPath
import my.drivebit.web.shouldShowStaticHeroShell
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

private const val NEARBY_LIST_PAGE_SIZE = 9

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val mapViewModel: MapViewModel = koinInject()
    val carSearchViewModel: CarSearchViewModel = koinInject()
    val mainContentViewModel: MainContentViewModel = koinInject()
    val currentFiltersRepository: CurrentFiltersRepository = koinInject(named("main"))
    val navigationState: NavigationState = koinInject()
    val navigationController = LocalNavigationController.current

    val state = filterViewModel.state.collectAsState()
    val currentPath by navigationState.currentPath.collectAsState()
    val mapState = mapViewModel.state.collectAsState()
    val carSearchState = carSearchViewModel.state.collectAsState()
    val firstListState by mainContentViewModel.firstList.collectAsState()
    val displayedCars by mainContentViewModel.displayedCars.collectAsState()
    val paginationInfo by mainContentViewModel.paginationInfo.collectAsState()
    val cars =
        when (val listState = firstListState) {
            is MainContentListState.FirstList -> listState.cars
            else -> emptyList()
        }
    val filters = state.value.filters
    val selectedFromPath =
        parseFilterSlugFromCityPath(currentPath)?.let { filterSlug ->
            val fromSegment = filterTitleFromPathSegment(filterSlug)
            if (fromSegment != "Все") {
                filters.firstOrNull { it.title == fromSegment }?.title
            } else {
                filters.firstOrNull { filterTitleToPathSegment(it.title) == filterSlug }?.title
            }
        }
    val selected = selectedFromPath ?: state.value.selected

    val startDateViewModel: DateFieldViewModel = koinInject(named("startDate"))
    val endDateViewModel: DateFieldViewModel = koinInject(named("endDate"))
    val myCityViewModel: MyCityViewModel = koinInject()
    val cityName by myCityViewModel.myCity.collectAsState(initial = "")

    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()

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

    val hadSearchParams = remember { mutableStateOf(false) }
    LaunchedEffect(selected, startState.date, endState.date) {
        if (hadSearchParams.value) {
            mainContentViewModel.markSearchStarted()
        }
        hadSearchParams.value = true
    }

    HtmlHeroDatesBridge(
        currentFiltersRepository = currentFiltersRepository,
        startDateViewModel = startDateViewModel,
        endDateViewModel = endDateViewModel,
    )

    HtmlFiltersBridge(
        filterViewModel = filterViewModel,
        navigationState = navigationState,
    )

    val staticHeroMounted = remember { document.getElementById("drivebit-hero-static") != null }
    val staticFiltersMounted = remember { document.getElementById("drivebit-filters-static") != null }
    val showComposeTripFilters =
        staticHeroMounted &&
            shouldShowStaticHeroShell(currentPath) &&
            !staticFiltersMounted

    AppWithHeader {
        if (showComposeTripFilters) {
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
                                    val targetUrl = targetPath + window.location.search
                                    window.history.pushState(null, "", targetUrl)
                                    navigationState.updatePath(targetPath)
                                }
                            }
                            filterViewModel.onSelect(filter.title)
                        },
                    )
                }
            }
        }

        when (selected) {
            "Поблизости" -> {
                when (val listState = firstListState) {
                    MainContentListState.Loading -> {
                        CarsGridSkeleton()
                    }

                    is MainContentListState.Error -> {
                        TextError(listState.message)
                    }

                    is MainContentListState.FirstList -> {
                        val nearbyLayout = mapState.value.nearbyLayoutMode
                        val nearbyListPage = mapState.value.nearbyListPage
                        val nearbyRadiusKm = mapState.value.nearbyRadiusKm
                        val usedFallbackCenter = mapState.value.usedFallbackCenter

                        LaunchedEffect(selected) {
                            mapViewModel.initializeNearbySearch()
                        }

                        LaunchedEffect(cars) {
                            mapViewModel.syncNearbyListPageToTotalCount(cars.size, NEARBY_LIST_PAGE_SIZE)
                        }

                        val markers =
                            cars
                                .mapNotNull { car ->
                                    val lat = car.general.address.geoLat
                                    val lon = car.general.address.geoLon
                                    if (lat != null && lon != null) {
                                        MapMarker(
                                            id = car.id,
                                            location =
                                                Location(
                                                    latitude = lat,
                                                    longitude = lon,
                                                ),
                                            title = listOfNotNull(car.general.brandName).joinToString(" "),
                                        )
                                    } else {
                                        null
                                    }
                                }

                        LaunchedEffect(cars, nearbyLayout) {
                            if (nearbyLayout == NearbyLayoutMode.Map) {
                                mapViewModel.updateCameraPositionFromCars(cars)
                            }
                        }

                        NearbyMapListSwitcher(
                            mode = nearbyLayout,
                            onModeChange = { mapViewModel.setNearbyLayoutMode(it) },
                        )

                        NearbyRadiusSelector(
                            selectedRadiusKm = nearbyRadiusKm,
                            onRadiusChange = { mapViewModel.setNearbyRadiusKm(it) },
                        )

                        if (usedFallbackCenter) {
                            P({
                                style {
                                    marginTop(8.px)
                                    property("color", "#666")
                                    property("font-size", "14px")
                                }
                            }) {
                                Text("Геолокация недоступна — показаны авто рядом с Москвой")
                            }
                        }

                        when (nearbyLayout) {
                            NearbyLayoutMode.Map ->
                                NearbyMapView(
                                    cameraPosition = mapState.value.cameraPosition,
                                    markers = markers,
                                    onMarkerClick = { marker ->
                                        navigateToCarDetail(
                                            carId = marker.id,
                                            startDate = startState.date,
                                            endDate = endState.date,
                                        )
                                    },
                                    onCameraMove = { position ->
                                        mapViewModel.updateCameraPosition(position)
                                    },
                                )
                            NearbyLayoutMode.List -> {
                                val totalCount = cars.size
                                val totalPages =
                                    if (totalCount == 0) {
                                        0
                                    } else {
                                        (totalCount + NEARBY_LIST_PAGE_SIZE - 1) / NEARBY_LIST_PAGE_SIZE
                                    }
                                val pagedCars =
                                    cars.drop(nearbyListPage * NEARBY_LIST_PAGE_SIZE).take(NEARBY_LIST_PAGE_SIZE)
                                Div({
                                    style {
                                        width(100.percent)
                                        marginTop(20.px)
                                    }
                                }) {
                                    CarsGrid(
                                        cars = pagedCars,
                                        carHref = { car ->
                                            buildCarDetailUrl(
                                                carId = car.id,
                                                startDate = startState.date,
                                                endDate = endState.date,
                                            )
                                        },
                                    )
                                    PaginationBar(
                                        currentPage = nearbyListPage,
                                        totalPages = totalPages,
                                        totalCount = totalCount,
                                        pageSize = NEARBY_LIST_PAGE_SIZE,
                                        onPageChange = { mapViewModel.setNearbyListPage(it) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                MainContentCarsSection(
                    firstListState = firstListState,
                    displayedCars = displayedCars,
                    paginationInfo = paginationInfo,
                    startDate = startState.date,
                    endDate = endState.date,
                    onPageChange = { mainContentViewModel.setPage(it) },
                )
            }
        }
    }
}

@Composable
private fun MainContentCarsSection(
    firstListState: MainContentListState,
    displayedCars: List<CarItem>,
    paginationInfo: Triple<Int, Int, Int>,
    startDate: String?,
    endDate: String?,
    onPageChange: (Int) -> Unit,
) {
    Div({
        style {
            width(100.percent)
            marginTop(20.px)
        }
    }) {
        when (val listState = firstListState) {
            MainContentListState.Loading -> {
                CarsGridSkeleton()
            }

            is MainContentListState.Error -> {
                TextError(listState.message)
            }

            is MainContentListState.FirstList -> {
                CarsGrid(
                    cars = displayedCars,
                    carHref = { car ->
                        buildCarDetailUrl(
                            carId = car.id,
                            startDate = startDate,
                            endDate = endDate,
                        )
                    },
                )
                PaginationBar(
                    currentPage = paginationInfo.first,
                    totalPages = paginationInfo.second,
                    totalCount = paginationInfo.third,
                    pageSize = 9,
                    onPageChange = onPageChange,
                )
            }
        }
    }
}

@Composable
private fun NearbyMapView(
    cameraPosition: MapCameraPosition,
    markers: List<MapMarker>,
    onMarkerClick: (MapMarker) -> Unit,
    onCameraMove: (MapCameraPosition) -> Unit,
) {
    SideEffect {
        val styleId = "hide-leaflet-attribution"
        if (document.getElementById(styleId) == null) {
            val style = document.createElement("style")
            style.id = styleId
            style.textContent =
                """
                .leaflet-control-attribution {
                    display: none !important;
                }
                """.trimIndent()
            document.head?.appendChild(style)
        }
    }

    Div({
        style {
            width(100.percent)
            height(600.px)
            marginTop(20.px)
            borderRadius(8.px)
            position(Position.Relative)
            property("overflow", "hidden")
        }
    }) {
        MapView(
            cameraPosition = cameraPosition,
            markers = markers,
            onMarkerClick = onMarkerClick,
            onCameraMove = onCameraMove,
        )

        Div({
            style {
                position(Position.Absolute)
                bottom(0.px)
                left(0.px)
                right(0.px)
                height(30.px)
                backgroundColor(CSSColors.White)
                property("z-index", "1000")
                property("pointer-events", "none")
            }
        })
    }
}
