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
            brandId = 1,
            brandName = "BMW",
            modelId = 2,
            modelName = "X5",
            bodyType = "SUV",
            bodyTypeTranslate = "Внедорожник",
            driveType = "All",
            driveTypeTranslate = "Полный",
            engineType = "Gasoline",
            engineTypeTranslate = "Бензин",
            engineVolume = 3.0,
            productionYear = 2021,
            seatsCount = 5,
            licensePlate = "A123BC",
            ValidAddressString = "Москва, ул. Тестовая, д. 1",
            photos =
                listOf(
                    CarPhotoItem(
                        id = 1,
                        url = "https://example.com/photo1.jpg",
                        uploadDate = "2024-01-01T00:00:00Z",
                        carId = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                    ),
                    CarPhotoItem(
                        id = 2,
                        url = "https://example.com/photo2.jpg",
                        uploadDate = "2024-01-02T00:00:00Z",
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
            assertEquals("BMW", car.resolvedBrandName())
            assertEquals("X5", car.resolvedModelName())
            assertEquals(2021, car.resolvedProductionYear())
            assertEquals(2, car.photos.size)
            assertEquals("https://example.com/photo1.jpg", car.photos[0].url)
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
