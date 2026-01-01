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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MockCarServiceForEdit : Car {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var carResponse: CarDetailResponse =
        CarDetailResponse(
            id = "ef4d16a2-aeeb-457e-abbe-b72473634690",
            brandId = 1,
            brandName = "Acura",
            modelId = 2,
            modelName = "CL",
            bodyType = "SUV",
            bodyTypeTranslate = "Внедорожник",
            driveType = "Rear",
            driveTypeTranslate = "Задний",
            engineType = "Diesel",
            engineTypeTranslate = "Дизель",
            engineVolume = 3.0,
            productionYear = 2020,
            seatsCount = 5,
            licensePlate = "K128CT",
            ValidAddressString = "Test Address",
            photos = emptyList(),
        )

    override suspend fun getMyCars(): List<my.drivebit.network.services.CarListItemResponse> =
        throw NotImplementedError()

    override suspend fun getCar(carId: String): CarDetailResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return carResponse.copy(id = carId)
    }

    override suspend fun createCar(request: CarCreateRequest): my.drivebit.network.services.CarResponse =
        throw NotImplementedError()

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): my.drivebit.network.services.CarResponse = throw NotImplementedError()

    override suspend fun deleteCar(carId: String) {
        // Mock implementation
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarEditViewModelTest {
    @Test
    fun `initial state should not be Loading`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            val initialState = viewModel.state.value
            assertIs<CarEditState.Loading>(initialState)
        }

    @Test
    fun `loadCar should set state to Loading then Success with form data`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditState.Success>(state)
            val successState = state as CarEditState.Success
            val formData = successState.formData

            assertEquals("test-car-id", formData.carId)
            assertEquals("K128CT", formData.licensePlate)
            assertEquals(1, formData.brandId)
            assertEquals("Acura", formData.brandName)
            assertEquals("Acura", formData.brandSearch)
            assertEquals(2, formData.modelId)
            assertEquals("CL", formData.modelName)
            assertEquals("CL", formData.modelSearch)
            assertEquals("SUV", formData.bodyType)
            assertEquals("Внедорожник", formData.bodyTypeTranslate)
            assertEquals("Внедорожник", formData.bodyTypeSearch)
            assertEquals("Rear", formData.driveType)
            assertEquals("Задний", formData.driveTypeTranslate)
            assertEquals("Задний", formData.driveTypeSearch)
            assertEquals("Diesel", formData.engineType)
            assertEquals("Дизель", formData.engineTypeTranslate)
            assertEquals("Дизель", formData.engineTypeSearch)
            assertEquals("3.0", formData.engineVolume)
            assertEquals("2020", formData.productionYear)
            assertEquals("5", formData.seatsCount)
            assertEquals("Test Address", formData.address)
        }

    @Test
    fun `loadCar should set state to Error on network exception`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService =
                MockCarServiceForEdit().apply {
                    shouldThrowNetworkException = true
                    errorMessage = "Network error occurred"
                }
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditState.Error>(state)
            assertEquals("Network error occurred", state.message)
        }

    @Test
    fun `loadCar should set state to Error on generic exception`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService =
                MockCarServiceForEdit().apply {
                    shouldThrowError = true
                    errorMessage = "Generic error"
                }
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditState.Error>(state)
            assertEquals("Generic error", state.message)
        }

    @Test
    fun `updateLicensePlate should update form data`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            viewModel.updateLicensePlate("NEW123")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditState.Success>(state)
            assertEquals("NEW123", state.formData.licensePlate)
        }

    @Test
    fun `selectBrand should update brand fields`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            viewModel.selectBrand(10, "Toyota")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditState.Success>(state)
            assertEquals(10, state.formData.brandId)
            assertEquals("Toyota", state.formData.brandName)
            assertEquals("Toyota", state.formData.brandSearch)
        }

    @Test
    fun `saveCar should reload car data after save`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditState.Success
            assertFalse(initialState.isSaving)

            viewModel.saveCar()
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarEditState.Success>(finalState)
            assertFalse(finalState.isSaving)
        }

    @Test
    fun `hasChanges should be false after loading car`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            assertFalse(viewModel.hasChanges.value, "hasChanges should be false after loading")
        }

    @Test
    fun `hasChanges should be true after changing license plate`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            assertFalse(viewModel.hasChanges.value, "hasChanges should be false initially")

            viewModel.updateLicensePlate("NEW123")
            advanceUntilIdle()

            assertTrue(viewModel.hasChanges.value, "hasChanges should be true after changing license plate")
        }

    @Test
    fun `hasChanges should be true after changing engine volume`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            assertFalse(viewModel.hasChanges.value, "hasChanges should be false initially")

            viewModel.updateEngineVolume("5.0")
            advanceUntilIdle()

            assertTrue(viewModel.hasChanges.value, "hasChanges should be true after changing engine volume")
        }

    @Test
    fun `hasChanges should be true after changing production year`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            assertFalse(viewModel.hasChanges.value, "hasChanges should be false initially")

            viewModel.updateProductionYear("2021")
            advanceUntilIdle()

            assertTrue(viewModel.hasChanges.value, "hasChanges should be true after changing production year")
        }

    @Test
    fun `hasChanges should be false after changing and reverting to original value`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockCarService = MockCarServiceForEdit()
            val viewModel =
                CarEditViewModelImpl(
                    carService = mockCarService,
                    coroutineScope = testScope,
                )

            viewModel.loadCar("test-car-id")
            advanceUntilIdle()

            val originalLicensePlate = (viewModel.state.value as CarEditState.Success).formData.licensePlate

            viewModel.updateLicensePlate("NEW123")
            advanceUntilIdle()
            assertTrue(viewModel.hasChanges.value, "hasChanges should be true after changing")

            viewModel.updateLicensePlate(originalLicensePlate)
            advanceUntilIdle()
            assertFalse(viewModel.hasChanges.value, "hasChanges should be false after reverting to original value")
        }
}
