package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import my.drivebit.navigation.NavigationState
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.City
import my.drivebit.repositories.HomeSearchRequest
import my.drivebit.repositories.MyCityRepository
import my.drivebit.shell.AppWithHeader
import my.drivebit.utils.HOME_DEFAULT_NEARBY_RADIUS_KM
import my.drivebit.utils.HomeUrlParts
import my.drivebit.utils.buildHomeUrl
import my.drivebit.utils.parseHomeUrl
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentListState
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.NearbyLayoutMode
import my.drivebit.web.HtmlFiltersBridge
import my.drivebit.web.HtmlHeroDatesBridge
import my.drivebit.web.buildCarDetailUrl
import my.drivebit.web.currentHomeUrlParts
import my.drivebit.web.filterTitleFromPathSegment
import my.drivebit.web.filterTitleToPathSegment
import my.drivebit.web.navigateToCarDetail
import my.drivebit.web.parseCitySlugFromPath
import my.drivebit.web.pushHomeUrl
import my.drivebit.web.replaceHomeUrl
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
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.w3c.dom.events.Event

private const val NEARBY_LIST_PAGE_SIZE = 9

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val navigationState: NavigationState = koinInject()
    val myCityRepository: MyCityRepository = koinInject()

    val filterState = filterViewModel.state.collectAsState()
    val currentPath by navigationState.currentPath.collectAsState()
    val city by myCityRepository.getSelectedCity.collectAsState(
        initial = City(id = 0, name = ""),
    )

    var locationSearch by remember { mutableStateOf(window.location.search) }
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            locationSearch = window.location.search
            navigationState.updatePath(window.location.pathname)
        }
        window.addEventListener("popstate", listener)
        window.addEventListener("drivebit-home-url-changed", listener)
        window.addEventListener("drivebit-hero-dates-changed", listener)
        onDispose {
            window.removeEventListener("popstate", listener)
            window.removeEventListener("drivebit-home-url-changed", listener)
            window.removeEventListener("drivebit-hero-dates-changed", listener)
        }
    }

    val homeUrl = remember(currentPath, locationSearch) { parseHomeUrl(currentPath + locationSearch) }
    val filters = filterState.value.filters
    val selected = filterTitleFromPathSegment(homeUrl.filterSlug)

    val startDateViewModel: DateFieldViewModel = koinInject(named("startDate"))
    val endDateViewModel: DateFieldViewModel = koinInject(named("endDate"))
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()

    HtmlHeroDatesBridge(
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

    val searchRequest =
        remember(homeUrl, city.id, selected) {
            HomeSearchRequest(
                cityId = city.id.toString(),
                taskShortName = selected.takeIf { it != "Все" },
                dateFrom = homeUrl.startDate,
                dateTo = homeUrl.endDate,
                page = homeUrl.page,
                lat = homeUrl.lat,
                lon = homeUrl.lon,
                radiusKm = homeUrl.radiusKm ?: HOME_DEFAULT_NEARBY_RADIUS_KM,
            )
        }

    val searchKey =
        if (selected == "Поблизости") {
            listOf(
                homeUrl.citySlug,
                homeUrl.filterSlug,
                homeUrl.startDate,
                homeUrl.endDate,
                homeUrl.lat,
                homeUrl.lon,
                homeUrl.radiusKm,
                city.id,
            ).joinToString("|")
        } else {
            listOf(
                homeUrl.citySlug,
                homeUrl.filterSlug,
                homeUrl.startDate,
                homeUrl.endDate,
                homeUrl.page,
                city.id,
            ).joinToString("|")
        }

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
                                    if (filter.title != "Все" && filter.title == selected) {
                                        "Все"
                                    } else {
                                        filter.title
                                    }
                                val isNearby = effectiveTitle == "Поблизости"
                                val next =
                                    HomeUrlParts(
                                        citySlug = citySlug,
                                        filterSlug = filterTitleToPathSegment(effectiveTitle),
                                        startDate = homeUrl.startDate,
                                        endDate = homeUrl.endDate,
                                        page = 1,
                                        lat = if (isNearby) homeUrl.lat else null,
                                        lon = if (isNearby) homeUrl.lon else null,
                                        radiusKm = if (isNearby) homeUrl.radiusKm else null,
                                    )
                                val nextUrl = buildHomeUrl(next)
                                val nextPath = nextUrl.substringBefore('?')
                                if (nextUrl != window.location.pathname + window.location.search) {
                                    window.history.pushState(null, "", nextUrl)
                                    navigationState.updatePath(nextPath)
                                    locationSearch = window.location.search
                                }
                            }
                        },
                    )
                }
            }
        }

        if (city.id == 0) {
            CarsGridSkeleton()
            return@AppWithHeader
        }

        key(searchKey) {
            val onNavigatePage: (Int) -> Unit = { pageIndex ->
                val next = homeUrl.copy(page = pageIndex + 1)
                pushHomeUrl(next)
                navigationState.updatePath(buildHomeUrl(next).substringBefore('?'))
                locationSearch = window.location.search
            }
            val mainContentViewModel: MainContentViewModel =
                koinInject(parameters = { parametersOf(searchRequest, onNavigatePage) })

            val firstListState by mainContentViewModel.firstList.collectAsState()
            val displayedCars by mainContentViewModel.displayedCars.collectAsState()
            val paginationInfo by mainContentViewModel.paginationInfo.collectAsState()
            val cars =
                when (val listState = firstListState) {
                    is MainContentListState.FirstList -> listState.cars
                    else -> emptyList()
                }

            when (selected) {
                "Поблизости" -> {
                    NearbyHomeContent(
                        homeUrl = homeUrl,
                        cars = cars,
                        firstListState = firstListState,
                        startDate = startState.date,
                        endDate = endState.date,
                        navigationState = navigationState,
                        onLocationSearchChanged = { locationSearch = it },
                    )
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
}

@Composable
private fun NearbyHomeContent(
    homeUrl: HomeUrlParts,
    cars: List<CarItem>,
    firstListState: MainContentListState,
    startDate: String?,
    endDate: String?,
    navigationState: NavigationState,
    onLocationSearchChanged: (String) -> Unit,
) {
    val mapViewModel: MapViewModel =
        koinInject(
            parameters = {
                parametersOf(
                    { lat: Double, lon: Double, radiusKm: Int ->
                        val next =
                            currentHomeUrlParts().copy(
                                lat = lat,
                                lon = lon,
                                radiusKm = radiusKm,
                                page = 1,
                            )
                        replaceHomeUrl(next)
                        navigationState.updatePath(buildHomeUrl(next).substringBefore('?'))
                        onLocationSearchChanged(window.location.search)
                    },
                    { radiusKm: Int ->
                        val next =
                            currentHomeUrlParts().copy(
                                radiusKm = radiusKm,
                                page = 1,
                            )
                        pushHomeUrl(next)
                        navigationState.updatePath(buildHomeUrl(next).substringBefore('?'))
                        onLocationSearchChanged(window.location.search)
                    },
                    homeUrl.radiusKm ?: HOME_DEFAULT_NEARBY_RADIUS_KM,
                )
            },
        )
    val mapState = mapViewModel.state.collectAsState()

    LaunchedEffect(homeUrl.page) {
        mapViewModel.setNearbyListPage((homeUrl.page - 1).coerceAtLeast(0))
    }

    when (val listState = firstListState) {
        MainContentListState.Loading -> {
            CarsGridSkeleton()
            if (homeUrl.lat == null || homeUrl.lon == null) {
                LaunchedEffect(Unit) {
                    mapViewModel.initializeNearbySearch()
                }
            }
        }

        is MainContentListState.Error -> {
            TextError(listState.message)
        }

        is MainContentListState.FirstList -> {
            if (homeUrl.lat == null || homeUrl.lon == null) {
                LaunchedEffect(Unit) {
                    mapViewModel.initializeNearbySearch()
                }
            }

            val nearbyLayout = mapState.value.nearbyLayoutMode
            val nearbyListPage = mapState.value.nearbyListPage
            val nearbyRadiusKm = mapState.value.nearbyRadiusKm
            val usedFallbackCenter = mapState.value.usedFallbackCenter

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
                                startDate = startDate,
                                endDate = endDate,
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
                                    startDate = startDate,
                                    endDate = endDate,
                                )
                            },
                        )
                        PaginationBar(
                            currentPage = nearbyListPage,
                            totalPages = totalPages,
                            totalCount = totalCount,
                            pageSize = NEARBY_LIST_PAGE_SIZE,
                            onPageChange = { pageIndex ->
                                mapViewModel.setNearbyListPage(pageIndex)
                                val next = currentHomeUrlParts().copy(page = pageIndex + 1)
                                pushHomeUrl(next)
                                navigationState.updatePath(buildHomeUrl(next).substringBefore('?'))
                                onLocationSearchChanged(window.location.search)
                            },
                        )
                    }
                }
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
