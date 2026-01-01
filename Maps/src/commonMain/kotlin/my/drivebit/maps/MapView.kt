package my.drivebit.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.maps.models.MapMarker
import my.drivebit.maps.models.MapProvider

@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    cameraPosition: MapCameraPosition,
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: (MapMarker) -> Unit = {},
    onCameraMove: (MapCameraPosition) -> Unit = {},
    mapProvider: MapProvider = MapProvider.OPENSTREETMAP,
    apiKey: String? = null,
)
