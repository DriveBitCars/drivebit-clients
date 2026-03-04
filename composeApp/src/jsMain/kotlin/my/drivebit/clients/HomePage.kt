package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.document
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarsGrid
import my.drivebit.components.DateRangeSelector
import my.drivebit.components.PaginationBar
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.filterButton
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MapViewModel
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
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val mapViewModel: MapViewModel = koinInject()
    val carSearchViewModel: CarSearchViewModel = koinInject()
    val mainContentViewModel: MainContentViewModel = koinInject()
    val currentFiltersRepository: CurrentFiltersRepository = koinInject(named("main"))

    val state = filterViewModel.state.collectAsState()
    val mapState = mapViewModel.state.collectAsState()
    val carSearchState = carSearchViewModel.state.collectAsState()
    val cars by mainContentViewModel.firstList.collectAsState()
    val displayedCars by mainContentViewModel.displayedCars.collectAsState()
    val paginationInfo by mainContentViewModel.paginationInfo.collectAsState()
    val filters = state.value.filters
    val selected = state.value.selected

    val startDateViewModel: DateFieldViewModel = koinInject(named("startDate"))
    val endDateViewModel: DateFieldViewModel = koinInject(named("endDate"))

    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()

    LaunchedEffect(startState.date) {
        currentFiltersRepository.updateStartDate(startState.date)
    }

    LaunchedEffect(endState.date) {
        currentFiltersRepository.updateEndDate(endState.date)
    }

    AppWithHeader {
        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = filter.backgroundIcon,
                searchContent = {
                    DateRangeSelector(
                        startDateViewModel = startDateViewModel,
                        endDateViewModel = endDateViewModel,
                    )
                },
            )
        }

        FilterButtonsRow {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == selected,
                    onClick = {
                        filterViewModel.onSelect(filter.title)
                    },
                )
            }
        }

        when (selected) {
            "Поблизости" -> {
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
                            } else null
                        }

                LaunchedEffect(cars) {
                    mapViewModel.updateCameraPositionFromCars(cars)
                }
                NearbyMapView(
                    cameraPosition = mapState.value.cameraPosition,
                    markers = markers,
                    onMarkerClick = { marker ->
                        println("Clicked marker: ${marker.title}")
                    },
                    onCameraMove = { position ->
                        mapViewModel.updateCameraPosition(position)
                    },
                )
            }
            else -> {
                Div({
                    style {
                        width(100.percent)
                        marginTop(20.px)
                    }
                }) {
                    CarsGrid(cars = displayedCars)
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

