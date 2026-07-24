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
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import my.drivebit.utils.HOME_DEFAULT_NEARBY_RADIUS_KM

enum class NearbyLayoutMode {
    Map,
    List,
}

data class MapScreenState(
    val cameraPosition: MapCameraPosition = MapCameraPosition.default(),
    val nearbyLayoutMode: NearbyLayoutMode = NearbyLayoutMode.Map,
    val nearbyListPage: Int = 0,
    val nearbyRadiusKm: Int = HOME_DEFAULT_NEARBY_RADIUS_KM,
    val usedFallbackCenter: Boolean = false,
    val nearbyPagedCars: List<CarItem> = emptyList(),
    val nearbyTotalPages: Int = 0,
    val nearbyTotalCount: Int = 0,
)

class MapViewModel(
    private val locationManager: LocationManager,
    private val addressSuggestRepository: AddressSuggestRepository,
    private val cityName: String,
    private val onNearbyCenterReady: ((lat: Double, lon: Double, radiusKm: Int) -> Unit)? = null,
    private val onNearbyRadiusChanged: ((radiusKm: Int) -> Unit)? = null,
    initialRadiusKm: Int = HOME_DEFAULT_NEARBY_RADIUS_KM,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val viewModelScope = coroutineScope
    private var nearbyCars: List<CarItem> = emptyList()
    private var nearbyPageSize: Int = 1
    private var requestedNearbyListPage: Int = 0

    private val _state =
        MutableStateFlow(
            MapScreenState(
                nearbyRadiusKm = initialRadiusKm,
            ),
        )

    val state: StateFlow<MapScreenState>
        get() = _state.asStateFlow()

    fun initializeNearbySearch(cityCars: List<CarItem> = emptyList()) {
        viewModelScope.launch {
            val location = locationManager.getCurrentLocation()
            val center = location ?: averageCarLocation(cityCars) ?: resolveCityCenter()
            val usedFallback = location == null
            val radiusKm = _state.value.nearbyRadiusKm
            if (center != null) {
                onNearbyCenterReady?.invoke(center.latitude, center.longitude, radiusKm)
            }
            _state.update {
                it.copy(
                    cameraPosition =
                        center?.let { fallbackCenter ->
                            MapCameraPosition(
                                location = fallbackCenter,
                                zoom = 10f,
                            )
                        } ?: it.cameraPosition,
                    usedFallbackCenter = usedFallback,
                    nearbyListPage = 0,
                )
            }
        }
    }

    fun setNearbyRadiusKm(km: Int) {
        _state.update { it.copy(nearbyRadiusKm = km) }
        requestedNearbyListPage = 0
        updateNearbyPage(0)
        onNearbyRadiusChanged?.invoke(km)
    }

    fun setNearbyLayoutMode(mode: NearbyLayoutMode) {
        _state.update { it.copy(nearbyLayoutMode = mode) }
    }

    fun setNearbyListPage(page: Int) {
        requestedNearbyListPage = page.coerceAtLeast(0)
        updateNearbyPage(requestedNearbyListPage)
    }

    fun updateNearbyCars(
        cars: List<CarItem>,
        totalCount: Int,
        pageSize: Int,
    ) {
        nearbyCars = cars
        nearbyPageSize = pageSize.coerceAtLeast(1)
        updateNearbyPage(requestedNearbyListPage, totalCount)
    }

    fun updateCameraPosition(position: MapCameraPosition) {
        _state.update { it.copy(cameraPosition = position) }
    }

    fun updateCameraPositionFromCars(cars: List<CarItem>) {
        val center = averageCarLocation(cars) ?: return
        _state.update {
            it.copy(
                cameraPosition =
                    MapCameraPosition(
                        location = center,
                        zoom = it.cameraPosition.zoom,
                    ),
            )
        }
    }

    private fun averageCarLocation(cars: List<CarItem>): Location? {
        val validLocations =
            cars.mapNotNull { car ->
                val lat = car.general.address.geoLat
                val lon = car.general.address.geoLon
                if (lat != null && lon != null) lat to lon else null
            }
        if (validLocations.isEmpty()) return null

        val (latitudeSum, longitudeSum) =
            validLocations.fold(0.0 to 0.0) { sum, location ->
                (sum.first + location.first) to (sum.second + location.second)
            }
        return Location(
            latitude = latitudeSum / validLocations.size,
            longitude = longitudeSum / validLocations.size,
        )
    }

    private suspend fun resolveCityCenter(): Location? {
        if (cityName.isBlank()) return null
        val result = addressSuggestRepository.suggest(cityName)
        if (result !is ResultAddressSuggest.Success) return null
        return result.suggestions.firstNotNullOfOrNull { suggestion ->
            val latitude = suggestion.data?.geoLat?.toDoubleOrNull()
            val longitude = suggestion.data?.geoLon?.toDoubleOrNull()
            if (latitude != null && longitude != null) {
                Location(latitude = latitude, longitude = longitude)
            } else {
                null
            }
        }
    }

    private fun updateNearbyPage(
        requestedPage: Int,
        totalCount: Int = _state.value.nearbyTotalCount,
    ) {
        val totalPages =
            if (nearbyCars.isEmpty()) {
                0
            } else {
                (nearbyCars.size + nearbyPageSize - 1) / nearbyPageSize
            }
        val page =
            if (totalPages == 0) {
                0
            } else {
                requestedPage.coerceIn(0, totalPages - 1)
            }
        _state.update {
            it.copy(
                nearbyListPage = page,
                nearbyPagedCars = nearbyCars.drop(page * nearbyPageSize).take(nearbyPageSize),
                nearbyTotalPages = totalPages,
                nearbyTotalCount = totalCount,
            )
        }
    }
}
