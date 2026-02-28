package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import kotlinx.browser.document
import my.drivebit.design.CSSColors
import my.drivebit.maps.MapView
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.network.services.CarDetailResponse
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.Div

@Composable
@Suppress("FunctionName")
fun CarLocationMap(car: CarDetailResponse) {
    val carLat = car.general.address.geoLat
    val carLon = car.general.address.geoLon

    if (carLat == null || carLon == null || carLat == 0.0 || carLon == 0.0) {
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

    val mapContainerId = "car-location-map-${car.id}"

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

    Div(
        attrs = {
            id(mapContainerId)
            style {
                width(100.percent)
                height(400.px)
                borderRadius(8.px)
                property("overflow", "hidden")
                position(Position.Relative)
            }
        },
    ) {
        MapView(
            cameraPosition = cameraPosition,
            markers = listOf(marker),
            onMarkerClick = {},
            onCameraMove = {},
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
