package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarsGrid
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.HeroBanner
import my.drivebit.components.MainPromoSections
import my.drivebit.components.NearbyMapListSwitcher
import my.drivebit.components.NearbyRadiusSelector
import my.drivebit.components.PaginationBar
import my.drivebit.components.filterButton
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.web.navigateToCarDetail
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.MyCityViewModel
import my.drivebit.viewmodels.NearbyLayoutMode
import my.drivebit.web.cityPathWithFilter
import my.drivebit.web.filterTitleFromPathSegment
import my.drivebit.web.filterTitleToPathSegment
import my.drivebit.web.isCityHomePath
import my.drivebit.resources.ImagePaths.SEARCHBACKGROUND_CAR0_JPG
import my.drivebit.utils.resolveCityNameForMeta
import my.drivebit.web.parseCitySlugFromPath
import my.drivebit.web.parseFilterSlugFromCityPath
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
    val cars by mainContentViewModel.firstList.collectAsState()
    val displayedCars by mainContentViewModel.displayedCars.collectAsState()
    val paginationInfo by mainContentViewModel.paginationInfo.collectAsState()
    val filters = state.value.filters
    val selectedFromPath =
        parseFilterSlugFromCityPath(currentPath)?.let { filterSlug ->
            filters.firstOrNull { filterTitleToPathSegment(it.title) == filterSlug }?.title
        }
    val urlFilterTitle = filterTitleFromPathSegment(parseFilterSlugFromCityPath(currentPath))
    val selected = selectedFromPath ?: urlFilterTitle

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

    LaunchedEffect(currentPath) {
        val pathFilter = parseFilterSlugFromCityPath(currentPath)
        if (pathFilter == null && state.value.selected != "Все") {
            filterViewModel.onSelect("Все")
        }
    }

    LaunchedEffect(currentPath, cityName) {
        if (isCityHomePath(currentPath) && cityName.isNotEmpty()) {
            mainContentViewModel.refresh()
        }
    }

    AppWithHeader {
        val citySlug = parseCitySlugFromPath(currentPath).orEmpty()
        val filterSlug = parseFilterSlugFromCityPath(currentPath)
        val heroCityName = resolveCityNameForMeta(citySlug, cityName)
        val heroFilterTitle = filterTitleFromPathSegment(filterSlug)
        val heroFilter = filters.find { it.title == heroFilterTitle }
        HeroBanner(
            backgroundIconUrl = heroFilter?.backgroundIcon ?: SEARCHBACKGROUND_CAR0_JPG,
            cityName = heroCityName,
            citySlug = citySlug,
            filterSlug = filterSlug,
            filterTitle = heroFilterTitle,
            startDateViewModel = startDateViewModel,
            endDateViewModel = endDateViewModel,
        )

        FilterButtonsRow {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == urlFilterTitle,
                    onClick = {
                        parseCitySlugFromPath(currentPath)?.let { citySlug ->
                            val effectiveTitle =
                                if (filter.title != "Все" && filter.title == selected) "Все" else filter.title
                            val targetPath = cityPathWithFilter(citySlug, effectiveTitle)
                            if (targetPath != currentPath) {
                                window.history.pushState(null, "", targetPath)
                                navigationState.updatePath(targetPath)
                            }
                        }
                        filterViewModel.onSelect(filter.title)
                    },
                )
            }
        }

        when (selected) {
            "Поблизости" -> {
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
                                    navigationController = navigationController,
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
                                onCarClick = { car ->
                                    navigateToCarDetail(
                                        carId = car.id,
                                        startDate = startState.date,
                                        endDate = endState.date,
                                        navigationController = navigationController,
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
            else -> {
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
                                navigationController = navigationController,
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
        MainPromoSections()
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
