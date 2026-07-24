package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.maps.LocationManager
import my.drivebit.maps.models.Location
import my.drivebit.network.services.AddressData
import my.drivebit.network.services.AddressSuggestion
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import my.drivebit.utils.HOME_DEFAULT_NEARBY_RADIUS_KM
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    private class FakeLocationManager(
        private val location: Location? = null,
    ) : LocationManager {
        override suspend fun getCurrentLocation(): Location? = location

        override fun hasPermission(): Boolean = location != null

        override suspend fun requestPermission(): Boolean = location != null
    }

    private class FakeAddressSuggestRepository(
        private val latitude: String? = null,
        private val longitude: String? = null,
    ) : AddressSuggestRepository {
        var query: String? = null

        override suspend fun suggest(query: String): ResultAddressSuggest {
            this.query = query
            val data =
                if (latitude != null && longitude != null) {
                    AddressData(geoLat = latitude, geoLon = longitude)
                } else {
                    null
                }
            return ResultAddressSuggest.Success(
                suggestions = data?.let { listOf(AddressSuggestion(data = it)) }.orEmpty(),
            )
        }
    }

    @Test
    fun `initializeNearbySearch uses GPS and invokes center callback`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(Location(55.1, 37.2)),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    onNearbyCenterReady = { lat, lon, radiusKm ->
                        callback = Triple(lat, lon, radiusKm)
                    },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.initializeNearbySearch()
            advanceUntilIdle()

            assertEquals(Triple(55.1, 37.2, HOME_DEFAULT_NEARBY_RADIUS_KM), callback)
            assertFalse(viewModel.state.value.usedFallbackCenter)
            assertEquals(55.1, viewModel.state.value.cameraPosition.location.latitude)
            assertEquals(0, viewModel.state.value.nearbyListPage)
        }

    @Test
    fun `initializeNearbySearch falls back to current city cars when GPS missing`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val cityCars =
                listOf(
                    carAt(id = "1", latitude = 47.196907, longitude = 39.633015),
                    carAt(id = "2", latitude = 47.194317, longitude = 39.633697),
                )
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    onNearbyCenterReady = { lat, lon, radiusKm ->
                        callback = Triple(lat, lon, radiusKm)
                    },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.initializeNearbySearch(cityCars)
            advanceUntilIdle()

            assertEquals(
                Triple(47.195612, 39.633356, HOME_DEFAULT_NEARBY_RADIUS_KM),
                callback,
            )
            assertTrue(viewModel.state.value.usedFallbackCenter)
            assertEquals(47.195612, viewModel.state.value.cameraPosition.location.latitude)
            assertEquals(39.633356, viewModel.state.value.cameraPosition.location.longitude)
        }

    @Test
    fun `initializeNearbySearch resolves selected city when GPS and city cars missing`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val addressRepository = FakeAddressSuggestRepository(latitude = "47.2357", longitude = "39.7015")
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = addressRepository,
                    cityName = "Ростов-на-Дону",
                    onNearbyCenterReady = { lat, lon, radiusKm ->
                        callback = Triple(lat, lon, radiusKm)
                    },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.initializeNearbySearch(emptyList())
            advanceUntilIdle()

            assertEquals(Triple(47.2357, 39.7015, HOME_DEFAULT_NEARBY_RADIUS_KM), callback)
            assertEquals("Ростов-на-Дону", addressRepository.query)
            assertTrue(viewModel.state.value.usedFallbackCenter)
        }

    @Test
    fun `initializeNearbySearch does not search around Moscow when selected city cannot be resolved`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Неизвестный город",
                    onNearbyCenterReady = { lat, lon, radiusKm ->
                        callback = Triple(lat, lon, radiusKm)
                    },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.initializeNearbySearch(emptyList())
            advanceUntilIdle()

            assertNull(callback)
            assertTrue(viewModel.state.value.usedFallbackCenter)
        }

    @Test
    fun `setNearbyRadiusKm updates state resets list page and invokes callback`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callbackRadius: Int? = null
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(Location(55.1, 37.2)),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    onNearbyCenterReady = { _, _, radiusKm -> callbackRadius = radiusKm },
                    onNearbyRadiusChanged = { radiusKm -> callbackRadius = radiusKm },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.setNearbyListPage(3)
            viewModel.setNearbyRadiusKm(25)

            assertEquals(25, viewModel.state.value.nearbyRadiusKm)
            assertEquals(0, viewModel.state.value.nearbyListPage)
            assertEquals(25, callbackRadius)
        }

    @Test
    fun `nearby list state exposes paged cars and database count`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            val cars = (1..20).map { carAt(id = it.toString(), latitude = 47.2, longitude = 39.7) }
            viewModel.updateNearbyCars(cars = cars, totalCount = 42, pageSize = 9)
            viewModel.setNearbyListPage(2)

            assertEquals(2, viewModel.state.value.nearbyListPage)
            assertEquals(listOf("19", "20"), viewModel.state.value.nearbyPagedCars.map { it.id })
            assertEquals(3, viewModel.state.value.nearbyTotalPages)
            assertEquals(42, viewModel.state.value.nearbyTotalCount)
        }

    @Test
    fun `nearby deep link page is preserved until cars load`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.setNearbyListPage(2)
            val cars = (1..20).map { carAt(id = it.toString(), latitude = 47.2, longitude = 39.7) }
            viewModel.updateNearbyCars(cars = cars, totalCount = 20, pageSize = 9)

            assertEquals(2, viewModel.state.value.nearbyListPage)
            assertEquals(listOf("19", "20"), viewModel.state.value.nearbyPagedCars.map { it.id })
        }

    @Test
    fun `empty filtered cars keep current city camera position`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )
            viewModel.initializeNearbySearch(
                listOf(carAt(id = "1", latitude = 47.196907, longitude = 39.633015)),
            )
            advanceUntilIdle()

            viewModel.updateCameraPositionFromCars(emptyList())

            assertEquals(47.196907, viewModel.state.value.cameraPosition.location.latitude)
            assertEquals(39.633015, viewModel.state.value.cameraPosition.location.longitude)
        }

    @Test
    fun `setNearbyLayoutMode switches to List and keeps radius`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    addressSuggestRepository = FakeAddressSuggestRepository(),
                    cityName = "Ростов-на-Дону",
                    initialRadiusKm = 25,
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.setNearbyLayoutMode(NearbyLayoutMode.List)

            assertEquals(NearbyLayoutMode.List, viewModel.state.value.nearbyLayoutMode)
            assertEquals(25, viewModel.state.value.nearbyRadiusKm)
        }

    private fun carAt(
        id: String,
        latitude: Double,
        longitude: Double,
    ) = CarItem(
        id = id,
        general =
            CarGeneral(
                brandName = "Test",
                modelName = "Car",
                vin = "VIN$id",
                seats = 4,
                address =
                    CarAddress(
                        geoLat = latitude,
                        geoLon = longitude,
                    ),
            ),
    )
}
