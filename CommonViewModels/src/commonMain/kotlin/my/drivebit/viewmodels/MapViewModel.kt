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
import my.drivebit.maps.models.Location
import my.drivebit.maps.models.MapCameraPosition
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.CurrentFiltersRepository

enum class NearbyLayoutMode {
    Map,
    List,
}

data class MapScreenState(
    val cameraPosition: MapCameraPosition = MapCameraPosition.default(),
    val nearbyLayoutMode: NearbyLayoutMode = NearbyLayoutMode.Map,
    val nearbyListPage: Int = 0,
    val nearbyRadiusKm: Int = CurrentFiltersRepository.DEFAULT_NEARBY_RADIUS_KM,
    val usedFallbackCenter: Boolean = false,
)

class MapViewModel(
    private val locationManager: LocationManager,
    private val currentFiltersRepository: CurrentFiltersRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val viewModelScope = coroutineScope

    private val _state =
        MutableStateFlow(
            MapScreenState(
                nearbyRadiusKm = CurrentFiltersRepository.DEFAULT_NEARBY_RADIUS_KM,
            ),
        )

    val state: StateFlow<MapScreenState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            currentFiltersRepository.nearbyRadiusKm.collect { km ->
                _state.update { it.copy(nearbyRadiusKm = km) }
            }
        }
    }

    fun initializeNearbySearch() {
        viewModelScope.launch {
            val location = locationManager.getCurrentLocation()
            val center = location ?: Location.moscow()
            val usedFallback = location == null
            currentFiltersRepository.updateNearbySearchCenter(
                lat = center.latitude,
                lon = center.longitude,
            )
            _state.update {
                it.copy(
                    cameraPosition =
                        MapCameraPosition(
                            location = center,
                            zoom = 10f,
                        ),
                    usedFallbackCenter = usedFallback,
                    nearbyListPage = 0,
                )
            }
        }
    }

    fun setNearbyRadiusKm(km: Int) {
        currentFiltersRepository.updateNearbyRadiusKm(km)
        _state.update { it.copy(nearbyRadiusKm = km, nearbyListPage = 0) }
    }

    fun setNearbyLayoutMode(mode: NearbyLayoutMode) {
        _state.update { it.copy(nearbyLayoutMode = mode) }
    }

    fun setNearbyListPage(page: Int) {
        _state.update { it.copy(nearbyListPage = page.coerceAtLeast(0)) }
    }

    fun syncNearbyListPageToTotalCount(
        totalCount: Int,
        pageSize: Int,
    ) {
        if (totalCount <= 0) {
            if (_state.value.nearbyListPage != 0) {
                _state.update { it.copy(nearbyListPage = 0) }
            }
            return
        }
        val totalPages = (totalCount + pageSize - 1) / pageSize
        val maxPageIndex = (totalPages - 1).coerceAtLeast(0)
        val clamped = _state.value.nearbyListPage.coerceIn(0, maxPageIndex)
        if (clamped != _state.value.nearbyListPage) {
            _state.update { it.copy(nearbyListPage = clamped) }
        }
    }

    fun updateCameraPosition(position: MapCameraPosition) {
        _state.update { it.copy(cameraPosition = position) }
    }

    fun updateCameraPositionFromCars(cars: List<CarItem>) {
        val validCars =
            cars
                .mapNotNull { car ->
                    val lat = car.general.address.geoLat
                    val lon = car.general.address.geoLon
                    if (lat != null && lon != null) lat to lon else null
                }

        val targetPosition =
            if (validCars.isNotEmpty()) {
                val (avgLat, avgLon) =
                    validCars
                        .fold(0.0 to 0.0) { acc, next -> (acc.first + next.first) to (acc.second + next.second) }
                        .let { (sumLat, sumLon) ->
                            val count = validCars.size.toDouble()
                            (sumLat / count) to (sumLon / count)
                        }

                MapCameraPosition(
                    location =
                        Location(
                            latitude = avgLat,
                            longitude = avgLon,
                        ),
                    zoom = _state.value.cameraPosition.zoom,
                )
            } else {
                MapCameraPosition.default()
            }

        _state.update { it.copy(cameraPosition = targetPosition) }
    }
}
