package my.drivebit.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.maps.models.MapProvider
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.js.Json

@JsName("L")
external object Leaflet {
    fun map(
        id: String,
        options: Json = definedExternally,
    ): Map

    fun map(
        element: Element,
        options: Json = definedExternally,
    ): Map

    fun latLng(
        lat: Double,
        lng: Double,
    ): LatLng

    fun marker(
        latLng: LatLng,
        options: Json = definedExternally,
    ): Marker

    fun tileLayer(
        urlTemplate: String,
        options: Json = definedExternally,
    ): TileLayer

    fun icon(options: Json): dynamic
}

external interface Map {
    fun setView(
        center: LatLng,
        zoom: Number,
    )

    fun invalidateSize(animate: Boolean? = definedExternally)

    fun remove()

    fun on(
        event: String,
        handler: (dynamic) -> Unit,
    )

    fun addLayer(layer: dynamic)

    fun removeLayer(layer: dynamic)

    fun getCenter(): LatLng

    fun getZoom(): Number
}

external interface LatLng {
    val lat: Number
    val lng: Number
}

external interface Marker {
    fun addTo(map: Map): Marker

    fun remove()

    fun bindPopup(content: String): Marker

    fun on(
        event: String,
        handler: (dynamic) -> Unit,
    )

    fun getLatLng(): LatLng
}

external interface TileLayer {
    fun addTo(map: Map): TileLayer
}

private var leafletMarkerIconsConfigured = false

private fun createLeafletMarkerIcon(): dynamic {
    val origin = window.location.origin
    val iconOptions = js("{}").unsafeCast<Json>()
    iconOptions.asDynamic().iconUrl = "$origin/vendor/leaflet/images/marker-icon.png"
    iconOptions.asDynamic().iconRetinaUrl = "$origin/vendor/leaflet/images/marker-icon-2x.png"
    iconOptions.asDynamic().shadowUrl = "$origin/vendor/leaflet/images/marker-shadow.png"
    iconOptions.asDynamic().iconSize = arrayOf<Any>(25, 41)
    iconOptions.asDynamic().iconAnchor = arrayOf<Any>(12, 41)
    iconOptions.asDynamic().popupAnchor = arrayOf<Any>(0, -41)
    return Leaflet.icon(iconOptions)
}

private fun configureLeafletMarkerIcons() {
    if (leafletMarkerIconsConfigured || !js("typeof L !== 'undefined'").unsafeCast<Boolean>()) {
        return
    }
    js("delete L.Icon.Default.prototype._getIconUrl")
    val origin = window.location.origin
    val iconOptions = js("{}").unsafeCast<Json>()
    iconOptions.asDynamic().iconUrl = "$origin/vendor/leaflet/images/marker-icon.png"
    iconOptions.asDynamic().iconRetinaUrl = "$origin/vendor/leaflet/images/marker-icon-2x.png"
    iconOptions.asDynamic().shadowUrl = "$origin/vendor/leaflet/images/marker-shadow.png"
    val iconDefault = js("L.Icon.Default").unsafeCast<dynamic>()
    iconDefault.mergeOptions(iconOptions)
    leafletMarkerIconsConfigured = true
}

@Composable
actual fun MapView(
    modifier: Modifier,
    cameraPosition: MapCameraPosition,
    markers: List<MapMarker>,
    onMarkerClick: (MapMarker) -> Unit,
    onCameraMove: (MapCameraPosition) -> Unit,
    mapProvider: MapProvider,
    apiKey: String?,
) {
    val mapId = remember { "map-${kotlin.random.Random.nextInt(100000)}" }
    val mapContainer = remember { mutableMapOf<String, Any?>() }
    val leafletMarkers = remember { mutableListOf<Marker>() }

    Div(
        attrs = {
            id(mapId)
            style {
                width(100.percent)
                height(100.percent)
                property("position", "relative")
            }
        },
    )

    SideEffect {
        val container = document.getElementById(mapId) as? HTMLElement
        if (container != null && js("typeof L !== 'undefined'").unsafeCast<Boolean>()) {
            configureLeafletMarkerIcons()
            val existingMap = mapContainer["map"] as? Map
            if (existingMap == null) {
                val mapOptions = js("{}").unsafeCast<Json>()
                mapOptions.asDynamic().zoomControl = true

                val map = Leaflet.map(container, mapOptions)
                mapContainer["map"] = map

                val tileLayerOptions = js("{}").unsafeCast<Json>()
                val tileLayerUrl: String
                val attribution: String

                when (mapProvider) {
                    MapProvider.YANDEX_MAPS -> {
                        if (apiKey == null) {
                            console.warn("Yandex Maps requires API key. Falling back to OpenStreetMap.")
                            attribution = "© OpenStreetMap contributors"
                            tileLayerUrl = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        } else {
                            attribution = "© Яндекс.Карты"
                            tileLayerOptions.asDynamic().maxZoom = 19
                            tileLayerUrl =
                                "https://core-renderer-tiles.maps.yandex.net/tiles?l=map&v=21.03.15-0&x={x}&y={y}&z={z}&key=$apiKey"
                        }
                    }
                    MapProvider.OPENSTREETMAP -> {
                        attribution = "© OpenStreetMap contributors"
                        tileLayerOptions.asDynamic().maxZoom = 19
                        tileLayerUrl = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    }
                }

                tileLayerOptions.asDynamic().attribution = attribution

                val tileLayer = Leaflet.tileLayer(tileLayerUrl, tileLayerOptions)
                tileLayer.addTo(map)

                window.setTimeout(
                    {
                        map.invalidateSize()
                    },
                    0,
                )

                mapContainer["moveTimeout"] = null
                map.on("moveend") {
                    (mapContainer["moveTimeout"] as? Int)?.let { window.clearTimeout(it) }
                    val timeoutId =
                        window.setTimeout({
                            val currentMap = mapContainer["map"] as? Map
                            if (currentMap != null && currentMap == map) {
                                val center = map.getCenter()
                                val zoom = map.getZoom()
                                onCameraMove(
                                    MapCameraPosition(
                                        location =
                                            my.drivebit.maps.models.Location(
                                                latitude = center.lat.toDouble(),
                                                longitude = center.lng.toDouble(),
                                            ),
                                        zoom = zoom.toFloat(),
                                    ),
                                )
                            }
                        }, 300)
                    mapContainer["moveTimeout"] = timeoutId
                }
            }
        }
    }

    SideEffect {
        val map = mapContainer["map"] as? Map
        if (map != null) {
            val center =
                Leaflet.latLng(
                    cameraPosition.location.latitude,
                    cameraPosition.location.longitude,
                )
            map.setView(center, cameraPosition.zoom.toInt())
            window.setTimeout(
                {
                    map.invalidateSize()
                },
                0,
            )
        }
    }

    SideEffect {
        val map = mapContainer["map"] as? Map
        if (map != null) {
            leafletMarkers.forEach { it.remove() }
            leafletMarkers.clear()

            markers.forEach { marker ->
                val markerLatLng =
                    Leaflet.latLng(
                        marker.location.latitude,
                        marker.location.longitude,
                    )
                val markerOptions = js("{}").unsafeCast<Json>()
                markerOptions.asDynamic().icon = createLeafletMarkerIcon()
                val leafletMarker = Leaflet.marker(markerLatLng, markerOptions)
                leafletMarker.addTo(map)

                if (marker.title != null) {
                    leafletMarker.bindPopup(marker.title)
                }

                leafletMarker.on("click") {
                    onMarkerClick(marker)
                }

                leafletMarkers.add(leafletMarker)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            (mapContainer["moveTimeout"] as? Int)?.let { window.clearTimeout(it) }
            leafletMarkers.forEach { it.remove() }
            leafletMarkers.clear()
            val map = mapContainer["map"] as? Map
            map?.remove()
            mapContainer.clear()
        }
    }
}
