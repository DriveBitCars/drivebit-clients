package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarItemSmall
import my.drivebit.components.Column
import my.drivebit.components.DateRangeSelector
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.Row
import my.drivebit.components.filterButton
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.network.services.CarItem
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.viewmodels.MapViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.overflowX
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun HomePage() {
    val filterViewModel: FiltersViewModel = koinInject()
    val mapViewModel: MapViewModel = koinInject()
    val carSearchViewModel: CarSearchViewModel = koinInject()
    val mainContentViewModel: MainContentViewModel = koinInject()

    val state = filterViewModel.state.collectAsState()
    val mapState = mapViewModel.state.collectAsState()
    val carSearchState = carSearchViewModel.state.collectAsState()
    val cars by mainContentViewModel.firstList.collectAsState()
    val filters = state.value.filters
    val selected = state.value.selected

    val startDateViewModel: DateFieldViewModel = koinInject(named("startDate"))
    val endDateViewModel: DateFieldViewModel = koinInject(named("endDate"))

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
                    cars.map { car ->
                        val lat = car.general.address.geoLat
                        val lon = car.general.address.geoLon
                        MapMarker(
                            id = car.id,
                            location =
                                Location(
                                    latitude = lat,
                                    longitude = lon,
                                ),
                            title = listOfNotNull(car.general.brandName).joinToString(" "),
                        )
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
                CarsListView(cars = cars)
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
    Div({
        style {
            width(100.percent)
            height(600.px)
            marginTop(20.px)
            borderRadius(8.px)
        }
    }) {
        MapView(
            cameraPosition = cameraPosition,
            markers = markers,
            onMarkerClick = onMarkerClick,
            onCameraMove = onCameraMove,
        )
    }
}

@Composable
private fun CarsListView(cars: List<CarItem>) {
    val scrollContainerId = "cars-scroll-container"

    Div({
        style {
            width(100.percent)
            marginTop(20.px)
        }
    }) {
        Row(
            justifyContent = JustifyContent.SpaceBetween,
            alignItems = AlignItems.Center,
            modifier = {
                width(100.percent)
                marginBottom(16.px)
            },
        ) {
            Div()

            if (cars.isNotEmpty()) {
                Row(gap = 8.px) {
                    ScrollButton(
                        direction = "left",
                        onClick = {
                            val container =
                                kotlinx.browser.document.getElementById(
                                    scrollContainerId,
                                ) as? org.w3c.dom.HTMLElement
                            container?.scrollBy(-296.0, 0.0)
                        },
                    )

                    ScrollButton(
                        direction = "right",
                        onClick = {
                            val container =
                                kotlinx.browser.document.getElementById(
                                    scrollContainerId,
                                ) as? org.w3c.dom.HTMLElement
                            container?.scrollBy(296.0, 0.0)
                        },
                    )
                }
            }
        }

        Row(
            gap = 16.px,
            modifier = {
                width(100.percent)
                overflowX("hidden")
            },
            attrs = { id(scrollContainerId) },
        ) {
            cars.forEach { car ->
                Column(
                    gap = 8.px,
                    modifier = {
                        flexShrink(0)
                        width(280.px)
                    },
                ) {
                    CarItemSmall(
                        car = car,
                        onClick = {
                            window.location.href = "/car-detail?id=${car.id}"
                        },
                    )
                    Div({
                        style {
                            paddingLeft(12.px)
                            paddingRight(12.px)
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun ScrollButton(
    direction: String,
    onClick: () -> Unit,
) {
    Row(
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.Center,
        modifier = {
            width(40.px)
            height(40.px)
            borderRadius(50.percent)
            backgroundColor(CSSColors.White)
            cursor("pointer")
        },
        attrs = {
            onClick { onClick() }
            onMouseEnter {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    "#f5f5f5",
                )
            }
            onMouseLeave {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                    "background-color",
                    CSSColors.WhiteString,
                )
            }
        },
    ) {
        Span({
            style {
                fontSize(20.px)
                fontWeight("bold")
                color(CSSColors.Black)
            }
        }) {
            Text(if (direction == "left") "‹" else "›")
        }
    }
}
