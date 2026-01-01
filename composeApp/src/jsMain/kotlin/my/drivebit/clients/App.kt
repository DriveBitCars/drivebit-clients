package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CenteredContent
import my.drivebit.components.FilterBackgroundImage
import my.drivebit.components.FilterButtonsRow
import my.drivebit.components.TextSmartHeader
import my.drivebit.components.filterButton
import my.drivebit.maps.MapView
import my.drivebit.navigation.Navigation
import my.drivebit.repositories.di.repositoriesModule
import my.drivebit.resources.ImagePaths
import my.drivebit.screens.AddressInputPage
import my.drivebit.screens.BodyTypeSelectionPage
import my.drivebit.screens.CarBrandSelectionPage
import my.drivebit.screens.CarEditPage
import my.drivebit.screens.CarModelSelectionPage
import my.drivebit.screens.CarPhotosPage
import my.drivebit.screens.CarPhotosUploadPage
import my.drivebit.screens.ChangeEmailPage
import my.drivebit.screens.ChangePhonePage
import my.drivebit.screens.CitySelectionPage
import my.drivebit.screens.DailyRateInputPage
import my.drivebit.screens.DriveTypeSelectionPage
import my.drivebit.screens.EditNamePage
import my.drivebit.screens.EngineTypeSelectionPage
import my.drivebit.screens.EngineVolumeInputPage
import my.drivebit.screens.HourlyRateInputPage
import my.drivebit.screens.LicensePlateInputPage
import my.drivebit.screens.ListYourCarPage
import my.drivebit.screens.LoginPage
import my.drivebit.screens.MyCarsPage
import my.drivebit.screens.OtpVerificationPage
import my.drivebit.screens.ProductionYearInputPage
import my.drivebit.screens.ProfilePage
import my.drivebit.screens.SeatsCountInputPage
import my.drivebit.shared.storage.di.storageModule
import my.drivebit.viewmodels.FiltersViewModel
import my.drivebit.viewmodels.MapViewModel
import my.drivebit.viewmodels.di.commonViewModelsModule
import my.drivebit.web.di.webModule
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
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
                currentPath == "/city-selection" -> {
                    CitySelectionPage(
                        onCitySelected = {
                            window.location.href = "/address-input"
                        },
                    )
                }
                currentPath == "/list-your-car" -> {
                    ListYourCarPage()
                }
                currentPath.startsWith("/verify-otp") -> {
                    OtpVerificationPage()
                }
                currentPath == "/login-by-phone" -> {
                    LoginPage(viewModelQualifier = named("phone"))
                }
                currentPath == "/login-by-mail" -> {
                    LoginPage(viewModelQualifier = named("email"))
                }
                currentPath == "/profile" -> {
                    ProfilePage()
                }
                currentPath == "/my-cars" -> {
                    MyCarsPage()
                }
                currentPath.startsWith("/car-edit") -> {
                    CarEditPage()
                }
                currentPath.startsWith("/car-photos") -> {
                    CarPhotosPage()
                }
                currentPath.startsWith("/edit-name") -> {
                    EditNamePage(currentPath)
                }
                currentPath == "/change-email" -> {
                    ChangeEmailPage()
                }
                currentPath == "/change-phone" -> {
                    ChangePhonePage()
                }
                currentPath == "/address-input" -> {
                    AddressInputPage(
                        onNavigateToWinCode = {
                            window.location.href = "/license-plate-input"
                        },
                    )
                }
                currentPath == "/license-plate-input" -> {
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
                currentPath == "/car-brand-selection" -> {
                    CarBrandSelectionPage(
                        onBrandSelected = { brandId ->
                            window.location.href = "/car-model-selection?brandId=$brandId"
                        },
                    )
                }
                currentPath == "/car-model-selection" -> {
                    CarModelSelectionPage(
                        onModelSelected = {
                            window.location.href = "/body-type-selection"
                        },
                    )
                }
                currentPath == "/body-type-selection" -> {
                    BodyTypeSelectionPage(
                        onBodyTypeSelected = {
                            window.location.href = "/drive-type-selection"
                        },
                    )
                }
                currentPath == "/drive-type-selection" -> {
                    DriveTypeSelectionPage(
                        onDriveTypeSelected = {
                            window.location.href = "/engine-type-selection"
                        },
                    )
                }
                currentPath == "/engine-type-selection" -> {
                    EngineTypeSelectionPage(
                        onEngineTypeSelected = {
                            window.location.href = "/engine-volume-input"
                        },
                    )
                }
                currentPath == "/engine-volume-input" -> {
                    EngineVolumeInputPage(
                        onVolumeEntered = {
                            window.location.href = "/production-year-input"
                        },
                    )
                }
                currentPath == "/production-year-input" -> {
                    ProductionYearInputPage(
                        onYearEntered = {
                            window.location.href = "/seats-count-input"
                        },
                    )
                }
                currentPath == "/seats-count-input" -> {
                    SeatsCountInputPage(
                        onSeatsCountEntered = {
                            window.location.href = "/hourly-rate-input"
                        },
                    )
                }
                currentPath == "/hourly-rate-input" -> {
                    HourlyRateInputPage(
                        onHourlyRateEntered = {
                            window.location.href = "/daily-rate-input"
                        },
                    )
                }
                currentPath == "/daily-rate-input" -> {
                    DailyRateInputPage(
                        onDailyRateEntered = {
                            kotlinx.browser.window.location.href = "/my-cars"
                        },
                    )
                }
                currentPath.startsWith("/car-photos-upload") -> {
                    CarPhotosUploadPage(
                        onPhotosUploaded = {
                            window.location.href = "/"
                        },
                    )
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

    val state = filterViewModel.state.collectAsState()
    val mapState = mapViewModel.state.collectAsState()
    val filters = state.value.filters
    val selected = state.value.selected

    AppWithHeader {
        val selectedFilter = filters.find { it.title == selected }
        selectedFilter?.let { filter ->
            FilterBackgroundImage(
                backgroundIconUrl = filter.backgroundIcon,
            )
        }

        FilterButtonsRow {
            filters.forEach { filter ->
                filterButton(
                    filter = filter,
                    isSelected = filter.title == selected,
                    onClick = {
                        filterViewModel.onSelect(filter.title)
                        if (filter.title == "По близости") {
                            mapViewModel.requestLocationForNearbyFilter()
                        }
                    },
                )
            }
        }

        if (selected == "По близости") {
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
                    cameraPosition = mapState.value.cameraPosition,
                    markers = emptyList(),
                    onMarkerClick = { marker ->
                        println("Clicked marker: ${marker.title}")
                    },
                    onCameraMove = { position ->
                        mapViewModel.updateCameraPosition(position)
                    },
                )
            }
        } else {
            CenteredContent {
                Img(
                    src = ImagePaths.FIX_SVG,
                    alt = "Coming soon",
                    attrs = {
                        style {
                            property("max-width", "600px")
                            width(100.percent)
                            property("height", "auto")
                            marginBottom(20.px)
                        }
                    },
                )
                TextSmartHeader("скоро...")
            }
        }
    }
}
