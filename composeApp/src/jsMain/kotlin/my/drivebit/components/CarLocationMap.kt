package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.network.services.CarDetailResponse
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
@Suppress("FunctionName")
fun CarLocationMap(car: CarDetailResponse) {
    val carLat = car.general.address.geoLat
    val carLon = car.general.address.geoLon

    if (carLat == 0.0 || carLon == 0.0) {
        return
    }

    val carLocation =
        Location(
            latitude = carLat,
            longitude = carLon,
        )

    val carName = "${car.resolvedBrandName()} ${car.resolvedModelName()}".trim()

    val cameraPosition =
        MapCameraPosition(
            location = carLocation,
            zoom = 15f,
        )

    val marker =
        MapMarker(
            id = car.id,
            location = carLocation,
            title = carName,
        )

    Div({
        style {
            width(100.percent)
            height(400.px)
            borderRadius(8.px)
            property("overflow", "hidden")
        }
    }) {
        MapView(
            cameraPosition = cameraPosition,
            markers = listOf(marker),
            onMarkerClick = {},
            onCameraMove = {},
        )
    }
}
