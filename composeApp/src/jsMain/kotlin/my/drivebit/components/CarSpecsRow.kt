package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.resources.ImagePaths.BACK_WHEEL_DRIVE_SVG
import my.drivebit.resources.ImagePaths.FUEL_PUMP_SVG
import my.drivebit.resources.ImagePaths.FUEL_SVG
import my.drivebit.resources.ImagePaths.MANUAL_GEAR_SVG
import my.drivebit.resources.ImagePaths.MENU_USER_SVG
import my.drivebit.resources.ImagePaths.PATH_DISTANCE_SVG
import my.drivebit.viewmodels.NumberFormatter
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px

@Composable
@Suppress("FunctionName")
fun CarSpecsRow(car: CarDetailResponse) {
    Row(
        gap = 12.px,
        flexWrap = FlexWrap.Wrap,
        modifier = { marginTop(16.px) },
    ) {
        val seatsCount = car.resolvedSeatsCount()
        if (seatsCount > 0) {
            CarSpecChip(
                iconPath = MENU_USER_SVG,
                text = "$seatsCount мест",
            )
        }

        val engineType = car.resolvedEngineTypeTranslate()
        if (engineType.isNotEmpty()) {
            CarSpecChip(
                iconPath = FUEL_PUMP_SVG,
                text = engineType,
            )
        }

        val engineVolume = car.resolvedEngineVolume()
        if (engineVolume > 0) {
            CarSpecChip(
                iconPath = FUEL_SVG,
                text = "${NumberFormatter.formatDouble(engineVolume)} л",
            )
        }

        val driveType = car.resolvedDriveTypeTranslate()
        if (driveType.isNotEmpty()) {
            CarSpecChip(
                iconPath = BACK_WHEEL_DRIVE_SVG,
                text = driveType,
            )
        }

        val transmissionType = car.chassis?.transmissionTranslate
        if (!transmissionType.isNullOrEmpty()) {
            CarSpecChip(
                iconPath = MANUAL_GEAR_SVG,
                text = transmissionType,
            )
        }

        val availableMileage = car.availableMileagePerDayKm
        if (availableMileage != null && availableMileage > 0) {
            CarSpecChip(
                iconPath = PATH_DISTANCE_SVG,
                text = "${NumberFormatter.formatInt(availableMileage)} км/день",
            )
        }
    }
}
