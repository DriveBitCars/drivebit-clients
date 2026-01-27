package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.network.services.CarDetailResponse
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px

@Composable
fun CarSpecsRow(car: CarDetailResponse) {
    Row(
        gap = 12.px,
        flexWrap = FlexWrap.Wrap,
        modifier = { marginTop(16.px) },
    ) {
        val seatsCount = car.resolvedSeatsCount()
        if (seatsCount > 0) {
            CarSpecChip(
                iconPath = "images/menu/user.svg",
                text = "$seatsCount мест",
            )
        }

        val engineType = car.resolvedEngineTypeTranslate()
        if (engineType.isNotEmpty()) {
            CarSpecChip(
                iconPath = "images/fuel-pump.svg",
                text = engineType,
            )
        }

        val engineVolume = car.resolvedEngineVolume()
        if (engineVolume > 0) {
            CarSpecChip(
                iconPath = "images/fuel.svg",
                text = "${engineVolume.toInt()} л",
            )
        }

        val transmissionType = car.chassis?.transmissionTranslate
        if (!transmissionType.isNullOrEmpty()) {
            CarSpecChip(
                iconPath = "images/back-wheel-drive.svg",
                text = transmissionType,
            )
        }
    }
}
