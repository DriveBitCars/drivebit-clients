package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarResponse
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.SeasonalPriceAdjustmentDto
import my.drivebit.network.services.UpdateCarRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeCarForSeasonalPricing : Car {
    var car: CarDetailResponse =
        CarDetailResponse(
            id = "car-1",
            seasonalPriceAdjustments =
                listOf(
                    SeasonalPriceAdjustmentDto(
                        id = "period-1",
                        startsAt = "2026-12-01T00:00:00Z",
                        endsAt = "2026-12-31T00:00:00Z",
                        percent = 15.0,
                    ),
                ),
        )
    var lastUpdate: UpdateCarRequest? = null
    var shouldFailLoad = false
    var shouldFailSave = false

    override suspend fun search(
        cityId: String,
        dateFrom: String?,
        dateTo: String?,
        availableMileagePerDayKmMin: Int?,
        dailyPriceMin: Int?,
        dailyPriceMax: Int?,
        yearMin: Int?,
        yearMax: Int?,
        seatsMin: Int?,
        seatsMax: Int?,
        bodyTypes: List<String>?,
        engineTypes: List<String>?,
        colors: List<String>?,
        brandId: Int?,
        modelId: Int?,
        driveTypes: List<String>?,
        allowedTravelDestinations: List<String>?,
        geoLat: Double?,
        geoLon: Double?,
        radiusKm: Double?,
        page: Int,
        pageSize: Int,
    ): CarSearchResponse = throw NotImplementedError()

    override suspend fun getMyCars(): List<CarItem> = throw NotImplementedError()

    override suspend fun getCar(id: String): CarDetailResponse {
        if (shouldFailLoad) {
            throw NetworkException(HttpStatusCode.InternalServerError, "Load failed")
        }
        return car
    }

    override suspend fun createCar(request: CarCreateRequest): CarResponse = throw NotImplementedError()

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse = throw NotImplementedError()

    override suspend fun updateCar(request: UpdateCarRequest): CarResponse {
        lastUpdate = request
        if (shouldFailSave) {
            throw NetworkException(HttpStatusCode.BadRequest, "Save failed")
        }
        return CarResponse(id = request.carId)
    }

    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarSeasonalPricingViewModelTest {
    @Test
    fun `load maps existing periods to local dates`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carService = FakeCarForSeasonalPricing()
            val viewModel =
                CarSeasonalPricingViewModelImpl(
                    carId = "car-1",
                    carService = carService,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarSeasonalPricingState.Ready>(state)
            assertEquals(1, state.periods.size)
            assertEquals("period-1", state.periods[0].id)
            assertEquals("2026-12-01", state.periods[0].startsAt)
            assertEquals("2026-12-31", state.periods[0].endsAt)
            assertEquals("15", state.periods[0].percent)
        }

    @Test
    fun `addPeriod appends empty row`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                CarSeasonalPricingViewModelImpl(
                    carId = "car-1",
                    carService = FakeCarForSeasonalPricing(),
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.addPeriod()

            val state = viewModel.state.value as CarSeasonalPricingState.Ready
            assertEquals(2, state.periods.size)
            assertEquals("", state.periods.last().startsAt)
            assertEquals("", state.periods.last().endsAt)
            assertEquals("0", state.periods.last().percent)
        }

    @Test
    fun `save sends replacement list with iso dates`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carService = FakeCarForSeasonalPricing()
            val viewModel =
                CarSeasonalPricingViewModelImpl(
                    carId = "car-1",
                    carService = carService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            viewModel.save()
            advanceUntilIdle()

            val sent = carService.lastUpdate
            assertEquals("car-1", sent?.carId)
            assertEquals(1, sent?.seasonalPriceAdjustments?.size)
            assertEquals("2026-12-01T00:00:00.000Z", sent?.seasonalPriceAdjustments?.first()?.startsAt)
            assertEquals("2026-12-31T00:00:00.000Z", sent?.seasonalPriceAdjustments?.first()?.endsAt)
            assertEquals(15.0, sent?.seasonalPriceAdjustments?.first()?.percent)
            val ready = viewModel.state.value as CarSeasonalPricingState.Ready
            assertTrue(ready.saved)
        }

    @Test
    fun `save shows validation error and does not call api`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carService = FakeCarForSeasonalPricing()
            val viewModel =
                CarSeasonalPricingViewModelImpl(
                    carId = "car-1",
                    carService = carService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            val key = (viewModel.state.value as CarSeasonalPricingState.Ready).periods.first().key
            viewModel.updatePeriod(key = key, endsAt = "2026-11-01")
            viewModel.save()
            advanceUntilIdle()

            assertNull(carService.lastUpdate)
            val ready = viewModel.state.value as CarSeasonalPricingState.Ready
            assertEquals("Дата окончания не может быть раньше даты начала", ready.formError)
        }

    @Test
    fun `save empty list clears periods on backend`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carService = FakeCarForSeasonalPricing()
            val viewModel =
                CarSeasonalPricingViewModelImpl(
                    carId = "car-1",
                    carService = carService,
                    coroutineScope = testScope,
                )
            advanceUntilIdle()

            val key = (viewModel.state.value as CarSeasonalPricingState.Ready).periods.first().key
            viewModel.removePeriod(key)
            viewModel.save()
            advanceUntilIdle()

            assertEquals(emptyList(), carService.lastUpdate?.seasonalPriceAdjustments)
        }
}
