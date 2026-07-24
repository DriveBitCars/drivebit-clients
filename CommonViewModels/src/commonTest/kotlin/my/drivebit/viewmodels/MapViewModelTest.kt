package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.maps.LocationManager
import my.drivebit.maps.models.Location
import my.drivebit.utils.HOME_DEFAULT_NEARBY_RADIUS_KM
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `initializeNearbySearch uses GPS and invokes center callback`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(Location(55.1, 37.2)),
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
    fun `initializeNearbySearch falls back to Moscow when GPS missing`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var callback: Triple<Double, Double, Int>? = null
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    onNearbyCenterReady = { lat, lon, radiusKm ->
                        callback = Triple(lat, lon, radiusKm)
                    },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.initializeNearbySearch()
            advanceUntilIdle()

            val moscow = Location.moscow()
            assertEquals(Triple(moscow.latitude, moscow.longitude, HOME_DEFAULT_NEARBY_RADIUS_KM), callback)
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
    fun `syncNearbyListPageToTotalCount clamps page`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.setNearbyListPage(10)
            viewModel.syncNearbyListPageToTotalCount(totalCount = 20, pageSize = 9)

            assertEquals(2, viewModel.state.value.nearbyListPage)
        }

    @Test
    fun `setNearbyLayoutMode switches to List and keeps radius`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                MapViewModel(
                    locationManager = FakeLocationManager(null),
                    initialRadiusKm = 25,
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            viewModel.setNearbyLayoutMode(NearbyLayoutMode.List)

            assertEquals(NearbyLayoutMode.List, viewModel.state.value.nearbyLayoutMode)
            assertEquals(25, viewModel.state.value.nearbyRadiusKm)
        }
}
