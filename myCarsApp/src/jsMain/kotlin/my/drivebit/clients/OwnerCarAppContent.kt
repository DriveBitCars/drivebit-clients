package my.drivebit.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import my.drivebit.screens.AddressInputPage
import my.drivebit.screens.BodyTypeSelectionPage
import my.drivebit.screens.CarAvailabilityPage
import my.drivebit.screens.CarBrandSelectionPage
import my.drivebit.screens.CarEditPage
import my.drivebit.screens.CarModelSelectionPage
import my.drivebit.screens.CarPhotosPage
import my.drivebit.screens.CarPhotosUploadPage
import my.drivebit.screens.CarStsUploadPage
import my.drivebit.screens.CitySelectionMode
import my.drivebit.screens.CitySelectionPage
import my.drivebit.screens.DailyRateInputPage
import my.drivebit.screens.DescriptionInputPage
import my.drivebit.screens.DriveTypeSelectionPage
import my.drivebit.screens.EngineTypeSelectionPage
import my.drivebit.screens.EngineVolumeInputPage
import my.drivebit.screens.LicensePlateInputPage
import my.drivebit.screens.MyCarsPage
import my.drivebit.screens.PassportUploadPage
import my.drivebit.screens.ProductionYearInputPage
import my.drivebit.screens.SeatsCountInputPage
import my.drivebit.screens.TrunkSizeSelectionPage
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref

@Composable
internal fun OwnerCarAppContent(
    currentPath: String,
    storage: Storage,
) {
    when {
        currentPath.startsWith("/create-car") -> {
            RedirectToPath("/address-input")
        }
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
        currentPath.startsWith("/my-cars") -> {
            MyCarsPage()
        }
        currentPath.startsWith("/car-edit") -> {
            CarEditPage()
        }
        currentPath.startsWith("/car-photos-upload") -> {
            CarPhotosUploadPage(
                onPhotosUploaded = {
                    window.location.href = homePathHref(storage)
                },
            )
        }
        currentPath.startsWith("/car-photos") -> {
            CarPhotosPage()
        }
        currentPath.startsWith("/car-availability") -> {
            CarAvailabilityPage()
        }
        currentPath.startsWith("/address-input") -> {
            AddressInputPage(
                onNavigateToWinCode = {
                    window.location.href = "/car-brand-selection"
                },
            )
        }
        currentPath.startsWith("/car-sts-upload") -> {
            CarStsUploadPage()
        }
        currentPath.startsWith("/license-plate-input") -> {
            LicensePlateInputPage(
                onLicensePlateEntered = { carId ->
                    window.location.href = "/car-sts-upload?carId=$carId"
                },
                onMissingPassport = {
                    window.location.href = "/passport-upload?autoCreate=1"
                },
                onBack = {
                    window.location.href = "/daily-rate-input"
                },
            )
        }
        currentPath.startsWith("/car-brand-selection") -> {
            CarBrandSelectionPage(
                onBrandSelected = { brandId ->
                    window.location.href = "/car-model-selection?brandId=$brandId"
                },
                onBack = {
                    window.location.href = "/address-input"
                },
            )
        }
        currentPath.startsWith("/car-model-selection") -> {
            CarModelSelectionPage(
                onModelSelected = {
                    window.location.href = "/body-type-selection"
                },
                onBack = {
                    window.location.href = "/car-brand-selection"
                },
            )
        }
        currentPath.startsWith("/body-type-selection") -> {
            BodyTypeSelectionPage(
                onBodyTypeSelected = {
                    window.location.href = "/drive-type-selection"
                },
                onBack = {
                    window.location.href = "/car-model-selection"
                },
            )
        }
        currentPath.startsWith("/drive-type-selection") -> {
            DriveTypeSelectionPage(
                onDriveTypeSelected = {
                    window.location.href = "/engine-type-selection"
                },
                onBack = {
                    window.location.href = "/body-type-selection"
                },
            )
        }
        currentPath.startsWith("/engine-type-selection") -> {
            EngineTypeSelectionPage(
                onEngineTypeSelected = {
                    window.location.href = "/engine-volume-input"
                },
                onBack = {
                    window.location.href = "/drive-type-selection"
                },
            )
        }
        currentPath.startsWith("/engine-volume-input") -> {
            EngineVolumeInputPage(
                onVolumeEntered = {
                    window.location.href = "/production-year-input"
                },
                onBack = {
                    window.location.href = "/engine-type-selection"
                },
            )
        }
        currentPath.startsWith("/production-year-input") -> {
            ProductionYearInputPage(
                onYearEntered = {
                    window.location.href = "/seats-count-input"
                },
                onBack = {
                    window.location.href = "/engine-volume-input"
                },
            )
        }
        currentPath.startsWith("/seats-count-input") -> {
            SeatsCountInputPage(
                onSeatsCountEntered = {
                    window.location.href = "/trunk-size-selection"
                },
                onBack = {
                    window.location.href = "/production-year-input"
                },
            )
        }
        currentPath.startsWith("/trunk-size-selection") -> {
            TrunkSizeSelectionPage(
                onTrunkSizeSelected = {
                    window.location.href = "/description-input"
                },
                onBack = {
                    window.location.href = "/seats-count-input"
                },
            )
        }
        currentPath.startsWith("/daily-rate-input") -> {
            DailyRateInputPage(
                onNavigateToLicensePlate = {
                    window.location.href = "/license-plate-input"
                },
                onBack = {
                    window.location.href = "/description-input"
                },
            )
        }
        currentPath.startsWith("/description-input") -> {
            DescriptionInputPage(
                onDescriptionEntered = {
                    window.location.href = "/daily-rate-input"
                },
                onBack = {
                    window.location.href = "/trunk-size-selection"
                },
            )
        }
        currentPath.startsWith("/passport-upload") -> {
            PassportUploadPage(
                onPassportUploaded = {
                    window.location.href = "/my-cars"
                },
                onCarCreated = { carId ->
                    window.location.href = "/car-sts-upload?carId=$carId"
                },
            )
        }
    }
}

@Composable
private fun RedirectToPath(path: String) {
    LaunchedEffect(path) {
        window.location.replace(path)
    }
}
