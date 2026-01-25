package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarPhotoItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockCarServiceForDetail : Car {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var carResponse: CarDetailResponse =
        CarDetailResponse(
            id = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
            hourlyRate = 100.0,
            dailyRate = 332.0,
            general =
                my.drivebit.network.services.CarGeneral(
                    brandName = "Adam",
                    modelName = "Revo",
                    year = 0,
                    licensePlate = "УАААК",
                    vin = "",
                    seats = 0,
                    mileage = 0,
                    description = null,
                    address =
                        CarAddress(
                            geoLat = 47.1968624,
                            geoLon = 39.6330034,
                        ),
                ),
            chassis =
                my.drivebit.network.services.CarChassis(
                    horsePower = 0,
                    engineVolume = 4.0,
                    hasStartStopSystem = false,
                    engineType = "Diesel",
                    engineTypeTranslate = "Дизель",
                    transmissionType = "Automatic",
                    transmissionTranslate = "Автоматическая",
                    driveType = "AllWheel",
                    driveTypeTranslate = "Полный",
                    steeringWheelSide = "Left",
                    steeringWheelSideTranslate = "Слева",
                ),
            body =
                my.drivebit.network.services.CarBody(
                    bodyType = "Crossover",
                    bodyTypeTranslate = "Кроссовер",
                    color = "White",
                    colorTranslate = "Белый",
                    carRoofType = "Hardtop",
                    carRoofTypeTranslate = "Сплошная",
                ),
            brandName = "Adam",
            modelName = "Revo",
            bodyType = "Crossover",
            bodyTypeTranslate = "Кроссовер",
            driveType = "AllWheel",
            driveTypeTranslate = "Полный",
            engineType = "Diesel",
            engineTypeTranslate = "Дизель",
            engineVolume = 4.0,
            productionYear = 0,
            seatsCount = 0,
            licensePlate = "УАААК",
            ValidAddressString = "Ростов-на-Дону, Жлобинский 25",
            photos =
                listOf(
                    CarPhotoItem(
                        id = 11,
                        url = "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/28005139-1f22-497d-84d8-6cba79b978f0_compressed.jpg",
                        uploadDate = "2026-01-04T19:23:58.147478Z",
                        carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                    ),
                    CarPhotoItem(
                        id = 12,
                        url = "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_compressed.jpg",
                        uploadDate = "2026-01-04T19:24:26.05663Z",
                        carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                    ),
                    CarPhotoItem(
                        id = 14,
                        url = "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/4c39d7ba-f09f-4514-b80f-662024e56395_compressed.jpg",
                        uploadDate = "2026-01-04T20:07:05.060429Z",
                        carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                    ),
                    CarPhotoItem(
                        id = 16,
                        url = "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/64fe5ca8-6d7a-4a08-8a0a-17f0a0f86511_compressed.jpg",
                        uploadDate = "2026-01-05T18:23:35.866216Z",
                        carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                    ),
                ),
        )

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
    ) = throw NotImplementedError()

    override suspend fun getMyCars(): List<my.drivebit.network.services.CarItem> = throw NotImplementedError()

    override suspend fun getCar(id: String): CarDetailResponse {
        if (id.isBlank()) {
            throw IllegalArgumentException("Car ID cannot be empty")
        }
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return carResponse.copy(id = id)
    }

    override suspend fun createCar(
        request: my.drivebit.network.services.CarCreateRequest,
    ): my.drivebit.network.services.CarResponse = throw NotImplementedError()

    override suspend fun createOrUpdateCar(
        request: my.drivebit.network.services.CarCreateRequest,
        carId: String?,
    ): my.drivebit.network.services.CarResponse = throw NotImplementedError()

    override suspend fun deleteCar(carId: String): Unit = throw NotImplementedError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarDetailViewModelTest {
    @Test
    fun `initial state should be Loading`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            val carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            val initialState = viewModel.state.value
            assertIs<CarDetailState.Loading>(initialState)
        }

    @Test
    fun `should load car successfully with valid ID`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            val carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarDetailState.Success>(state)
            val successState = state as CarDetailState.Success
            val car = successState.car

            assertEquals(carId, car.id)
            assertEquals("Adam", car.resolvedBrandName())
            assertEquals("Revo", car.resolvedModelName())
            assertEquals(0, car.resolvedProductionYear())
            assertEquals(4, car.photos.size)
            assertEquals(
                "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/28005139-1f22-497d-84d8-6cba79b978f0_compressed.jpg",
                car.photos[0].url,
            )
        }

    @Test
    fun `should handle empty car ID error`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            val carId = ""
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarDetailState.Error>(state)
            val errorState = state as CarDetailState.Error
            assertEquals("Car ID cannot be empty", errorState.message)
        }

    @Test
    fun `should handle network error`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            mockCarService.shouldThrowNetworkException = true
            mockCarService.networkExceptionStatusCode = HttpStatusCode.NotFound
            mockCarService.errorMessage = "Автомобиль не найден"
            val carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarDetailState.Error>(state)
            val errorState = state as CarDetailState.Error
            assertEquals("Автомобиль не найден", errorState.message)
        }

    @Test
    fun `should handle generic error`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            mockCarService.shouldThrowError = true
            mockCarService.errorMessage = "Unexpected error occurred"
            val carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarDetailState.Error>(state)
            val errorState = state as CarDetailState.Error
            assertEquals("Unexpected error occurred", errorState.message)
        }

    @Test
    fun `should handle 404 Not Found error`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForDetail()
            mockCarService.shouldThrowNetworkException = true
            mockCarService.networkExceptionStatusCode = HttpStatusCode.NotFound
            mockCarService.errorMessage = "404 Not Found"
            val carId = "invalid-car-id"
            val viewModel =
                CarDetailViewModelImpl(
                    carService = mockCarService,
                    carId = carId,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarDetailState.Error>(state)
            val errorState = state as CarDetailState.Error
            assertEquals("404 Not Found", errorState.message)
        }
}
