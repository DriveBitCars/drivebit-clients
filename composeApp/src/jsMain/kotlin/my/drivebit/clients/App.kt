package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarItemSmall
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.TextInputField
import my.drivebit.components.filterButton
import my.drivebit.viewmodels.CarSearchViewModel
import my.drivebit.viewmodels.MainContentViewModel
import my.drivebit.network.services.CarItem
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.screens.AddressInputPage
import my.drivebit.screens.BodyTypeSelectionPage
import my.drivebit.screens.CarBrandSelectionPage
import my.drivebit.screens.CarEditPage
import my.drivebit.screens.CarModelSelectionPage
import my.drivebit.screens.CarPhotosPage
import my.drivebit.screens.CarPhotosUploadPage
import my.drivebit.screens.CarDetailPage
import my.drivebit.screens.CarPhotosGalleryPage
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.CitySelectionMode
import my.drivebit.screens.CitySelectionPage
import my.drivebit.screens.MyCitySelectionPage
import my.drivebit.screens.DailyRateInputPage
import my.drivebit.screens.PassportUploadPage
import my.drivebit.screens.DriveTypeSelectionPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.EngineTypeSelectionPage
import my.drivebit.screens.EngineVolumeInputPage
// import my.drivebit.screens.HourlyRateInputPage
import my.drivebit.screens.LicensePlateInputPage
import my.drivebit.screens.ListYourCarPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.MyCarsPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.DocumentsPage
import my.drivebit.screens.ProductionYearInputPage
import my.drivebit.screens.ProfilePage
import my.drivebit.screens.SearchPage
import my.drivebit.screens.SeatsCountInputPage
import my.drivebit.shared.storage.Storage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Suppress("FunctionName")
actual fun App() {
    KoinApplication(application = {
        modules(
            storageModule,
            repositoriesModule,
            webModule,
            commonViewModelsModule,
        )
    }) {
        Navigation { currentPath ->
            when {
                currentPath.startsWith("/city-selection") -> {
                    CitySelectionPage(
                        mode =
                            CitySelectionMode.ForCarCreation(
                                onCitySelected = {
                                    window.location.href = "/address-input"
                                },
                            ),
                    )
                }
                currentPath.startsWith("/my-city-selection") -> {
                    MyCitySelectionPage()
                }
                currentPath.startsWith("/list-your-car") -> {
                    ListYourCarPage()
                }
                currentPath.startsWith("/verify-otp") -> {
                    OtpVerificationPage()
                }
                currentPath.startsWith("/login-by-phone") -> {
                    LoginPage(viewModelQualifier = named("phone"))
                }
                currentPath.startsWith("/login-by-mail") -> {
                    LoginPage(viewModelQualifier = named("email"))
                }
                currentPath.startsWith("/profile") -> {
                    val storage: Storage = koinInject()
                    if (storage.isLogined()) {
                        ProfilePage()
                    } else {
                        window.location.href = "/"
                    }
                }
                currentPath.startsWith("/my-cars") -> {
                    MyCarsPage()
                }
                currentPath.startsWith("/documents") -> {
                    DocumentsPage()
                }
                currentPath.startsWith("/car-edit") -> {
                    CarEditPage()
                }
                currentPath.startsWith("/car-photos-gallery") -> {
                    CarPhotosGalleryPage()
                }
                currentPath.startsWith("/car-photos-upload") -> {
                    CarPhotosUploadPage(
                        onPhotosUploaded = {
                            window.location.href = "/"
                        },
                    )
                }
                currentPath.startsWith("/car-photos") -> {
                    CarPhotosPage()
                }
                currentPath.startsWith("/edit-name") -> {
                    EditNamePage(currentPath)
                }
                currentPath.startsWith("/change-email") -> {
                    ChangeEmailPage()
                }
                currentPath.startsWith("/change-phone") -> {
                    ChangePhonePage()
                }
                currentPath.startsWith("/address-input") -> {
                    AddressInputPage(
                        onNavigateToWinCode = {
                            window.location.href = "/license-plate-input"
                        },
                    )
                }
                currentPath.startsWith("/license-plate-input") -> {
                    LicensePlateInputPage(
                        onLicensePlateEntered = {
                            window.location.href = "/car-brand-selection"
                        },
                    )
                }
                // currentPath == "/win-code-input" -> {
                //     WinCodeInputPage(
                //         onWinCodeEntered = {
                //             window.location.href = "/car-brand-selection"
                //         },
                //     )
                // }
                currentPath.startsWith("/car-brand-selection") -> {
                    CarBrandSelectionPage(
                        onBrandSelected = { brandId ->
                            window.location.href = "/car-model-selection?brandId=$brandId"
                        },
                    )
                }
                currentPath.startsWith("/car-model-selection") -> {
                    CarModelSelectionPage(
                        onModelSelected = {
                            window.location.href = "/body-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/body-type-selection") -> {
                    BodyTypeSelectionPage(
                        onBodyTypeSelected = {
                            window.location.href = "/drive-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/drive-type-selection") -> {
                    DriveTypeSelectionPage(
                        onDriveTypeSelected = {
                            window.location.href = "/engine-type-selection"
                        },
                    )
                }
                currentPath.startsWith("/engine-type-selection") -> {
                    EngineTypeSelectionPage(
                        onEngineTypeSelected = {
                            window.location.href = "/engine-volume-input"
                        },
                    )
                }
                currentPath.startsWith("/engine-volume-input") -> {
                    EngineVolumeInputPage(
                        onVolumeEntered = {
                            window.location.href = "/production-year-input"
                        },
                    )
                }
                currentPath.startsWith("/production-year-input") -> {
                    ProductionYearInputPage(
                        onYearEntered = {
                            window.location.href = "/seats-count-input"
                        },
                    )
                }
                currentPath.startsWith("/seats-count-input") -> {
                    SeatsCountInputPage(
                        onSeatsCountEntered = {
                            // window.location.href = "/hourly-rate-input"
                            window.location.href = "/daily-rate-input"
                        },
                    )
                }
                // currentPath == "/hourly-rate-input" -> {
                //     HourlyRateInputPage(
                //         onHourlyRateEntered = {
                //             window.location.href = "/daily-rate-input"
                //         },
                //     )
                // }
                currentPath.startsWith("/daily-rate-input") -> {
                    DailyRateInputPage(
                        onDailyRateEntered = {
                            kotlinx.browser.window.location.href = "/my-cars"
                        },
                        onMissingPassport = {
                            kotlinx.browser.window.location.href = "/passport-upload?autoCreate=1"
                        },
                    )
                }
                currentPath.startsWith("/passport-upload") -> {
                    PassportUploadPage(
                        onPassportUploaded = {
                            window.location.href = "/my-cars"
                        },
                    )
                }
                currentPath.startsWith("/search") -> {
                    SearchPage()
                }
                currentPath.startsWith("/car-detail") -> {
                    CarDetailPage()
                }
                else -> {
                    HomePage()
                }
            }
        }
    }
}

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

    var searchQuery by remember { mutableStateOf("") }

    AppWithHeader {
        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = filter.backgroundIcon,
                searchContent = {
                    Div({
                        style {
                            width(100.percent)
                        }
                    }) {
                        TextInputField(
                            label = "",
                            value = searchQuery,
                            onValueChange = { newValue ->
                                searchQuery = newValue
                            },
                            onFocus = {
                                window.location.href = "/search"
                            },
                        )
                    }
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
                        if (filter.title == "Поблизости") {
                            mapViewModel.requestLocationForNearbyFilter()
                        }
                    },
                )
            }
        }

        when (selected) {
            "Поблизости" -> {
                NearbyMapView(
                    cameraPosition = mapState.value.cameraPosition,
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
    onMarkerClick: (MapMarker) -> Unit,
    onCameraMove: (MapCameraPosition) -> Unit,
) {
    Div({
        style {
            width(100.percent)
            height(600.px)
            marginTop(20.px)
            borderRadius(8.px)
            property("overflow", "hidden")
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        }
    }) {
        MapView(
            cameraPosition = cameraPosition,
            markers = emptyList(),
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
        Div({
            style {
                width(100.percent)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(16.px)
            }
        }) {
            Div()

            if (cars.isNotEmpty()) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Row)
                        gap(8.px)
                    }
                }) {
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

        Div({
            id(scrollContainerId)
            style {
                width(100.percent)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                gap(16.px)
                overflowX("hidden")
                property("scroll-behavior", "smooth")
            }
        }) {
            cars.forEach { car ->
                Div({
                    style {
                        property("flex-shrink", "0")
                        width(280.px)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(8.px)
                    }
                }) {
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
    Div({
        style {
            width(40.px)
            height(40.px)
            borderRadius(50.percent)
            backgroundColor(CSSColors.White)
            property("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.15)")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            cursor("pointer")
            property("transition", "background-color 0.2s ease")
        }
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
    }) {
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
