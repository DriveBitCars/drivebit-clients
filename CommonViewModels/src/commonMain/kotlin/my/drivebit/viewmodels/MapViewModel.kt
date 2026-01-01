package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.drivebit.maps.LocationManager
import my.drivebit.maps.models.MapCameraPosition

data class MapScreenState(
    val cameraPosition: MapCameraPosition = MapCameraPosition.default(),
)

class MapViewModel(
    private val locationManager: LocationManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val viewModelScope = coroutineScope

    private val _state = MutableStateFlow(MapScreenState())

    val state: StateFlow<MapScreenState>
        get() = _state.asStateFlow()

    fun requestLocationForNearbyFilter() {
        viewModelScope.launch {
            val location = locationManager.getCurrentLocation()

            val cameraPosition =
                if (location != null) {
                    MapCameraPosition(
                        location = location,
                        zoom = 13f,
                    )
                } else {
                    MapCameraPosition.default()
                }

            _state.update {
                it.copy(
                    cameraPosition = cameraPosition,
                )
            }
        }
    }

    fun updateCameraPosition(position: MapCameraPosition) {
        _state.update { it.copy(cameraPosition = position) }
    }
}
