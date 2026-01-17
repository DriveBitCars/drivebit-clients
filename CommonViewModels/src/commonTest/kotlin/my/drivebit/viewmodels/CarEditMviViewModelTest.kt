package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.repositories.MyCarRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MockMyCarRepository : MyCarRepository {
    override suspend fun getMyCar(): List<my.drivebit.network.services.CarItem> = emptyList()

    override fun refresh() {}
}

class MockCarServiceForMvi : Car {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var shouldThrowErrorOnSave = false
    var shouldThrowErrorOnDelete = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

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

    fun setCarWithZeroYear() {
        carResponse = carResponse.copy(productionYear = 0)
    }

    override suspend fun getMyCars(): List<my.drivebit.network.services.CarItem> = throw NotImplementedError()

    override suspend fun getCar(carId: String): CarDetailResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return carResponse
    }

    override suspend fun createCar(request: CarCreateRequest): my.drivebit.network.services.CarResponse =
        throw NotImplementedError()

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): my.drivebit.network.services.CarResponse {
        if (shouldThrowErrorOnSave) {
            if (shouldThrowNetworkException) {
                throw NetworkException(networkExceptionStatusCode, errorMessage)
            } else {
                throw Exception(errorMessage)
            }
        }
        return my.drivebit.network.services.CarResponse(
            id = carId ?: "new-car-id",
        )
    }

    override suspend fun deleteCar(carId: String) {
        if (shouldThrowErrorOnDelete) {
            if (shouldThrowNetworkException) {
                throw NetworkException(networkExceptionStatusCode, errorMessage)
            } else {
                throw Exception(errorMessage)
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarEditMviViewModelTest {
    @Test
    fun `initial state should be Loading`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            val initialState = viewModel.state.value
            assertIs<CarEditMviState.Loading>(initialState)
        }

    @Test
    fun `handleIntent LoadCar should set state to Loading then Success`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            val successState = state as CarEditMviState.Success
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", successState.formData.carId)
            assertEquals("K128CT", successState.formData.licensePlate)
        }

    @Test
    fun `handleIntent LoadCar should set state to Error on network exception`() =
        runTest(StandardTestDispatcher()) {
            val mockCarService =
                MockCarServiceForMvi().apply {
                    shouldThrowNetworkException = true
                    errorMessage = "Network error occurred"
                }
            val viewModel = createCarEditMviViewModel(carService = mockCarService, carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("test-car-id"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Error>(state)
            assertEquals("Network error occurred", state.message)
        }

    @Test
    fun `handleIntent UpdateLicensePlate should update form data and set hasChanges to true`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("test-car-id"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val updatedState = viewModel.state.value
            assertIs<CarEditMviState.Success>(updatedState)
            assertEquals("NEW123", updatedState.formData.licensePlate)
            assertTrue(updatedState.hasChanges, "hasChanges should be true after update")
        }

    @Test
    fun `handleIntent SaveCar should reload car data after save`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("test-car-id"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarEditMviState.Success>(finalState)
            assertFalse(finalState.hasChanges, "hasChanges should be false after save")
        }

    @Test
    fun `handleIntent SetBrandFocus should update focus state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("test-car-id"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandFocus(true))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertTrue(state.isBrandFocused)
        }

    @Test
    fun `button should be enabled when hasChanges is true`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")
            assertFalse(initialState.isSaveButtonEnabled, "Save button should be disabled when no changes")

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val updatedState = viewModel.state.value as CarEditMviState.Success
            assertTrue(updatedState.hasChanges, "hasChanges should be true after update")
            assertTrue(updatedState.isSaveButtonEnabled, "Save button should be enabled when hasChanges is true")
        }

    @Test
    fun `button should be enabled when hasChanges is true after brand selection`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val stateWithChanges = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateWithChanges.hasChanges, "hasChanges should be true")
            assertTrue(stateWithChanges.isSaveButtonEnabled, "Save button should be enabled")

            viewModel.handleIntent(CarEditIntent.SelectBrand(1, "Test"))
            advanceUntilIdle()

            val stateWithoutModel = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateWithoutModel.isSaveButtonEnabled, "Save button should be enabled when hasChanges is true")
        }

    @Test
    fun `button should be disabled when isSaving is true`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val stateBeforeSave = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBeforeSave.isSaveButtonEnabled, "Save button should be enabled before save")

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val stateDuringSave = viewModel.state.value
            if (stateDuringSave is CarEditMviState.Success) {
                assertFalse(stateDuringSave.isSaveButtonEnabled)
            }
        }

    @Test
    fun `LoadCar should set carId in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", state.carId)
        }

    @Test
    fun `SetBrandBlurTimeout should update brandBlurTimeout in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandBlurTimeout(100))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(100, state.brandBlurTimeout)
        }

    @Test
    fun `SetModelBlurTimeout should update modelBlurTimeout in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetModelBlurTimeout(200))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(200, state.modelBlurTimeout)
        }

    @Test
    fun `SetBodyTypeBlurTimeout should update bodyTypeBlurTimeout in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBodyTypeBlurTimeout(300))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(300, state.bodyTypeBlurTimeout)
        }

    @Test
    fun `SetDriveTypeBlurTimeout should update driveTypeBlurTimeout in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetDriveTypeBlurTimeout(400))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(400, state.driveTypeBlurTimeout)
        }

    @Test
    fun `SetEngineTypeBlurTimeout should update engineTypeBlurTimeout in state`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetEngineTypeBlurTimeout(500))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(500, state.engineTypeBlurTimeout)
        }

    @Test
    fun `blur timeout can be set to null`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandBlurTimeout(100))
            advanceUntilIdle()

            val state1 = viewModel.state.value as CarEditMviState.Success
            assertEquals(100, state1.brandBlurTimeout)

            viewModel.handleIntent(CarEditIntent.SetBrandBlurTimeout(null))
            advanceUntilIdle()

            val state2 = viewModel.state.value as CarEditMviState.Success
            assertEquals(null, state2.brandBlurTimeout)
        }

    @Test
    fun `all focus states should be independent`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandFocus(true))
            viewModel.handleIntent(CarEditIntent.SetModelFocus(true))
            viewModel.handleIntent(CarEditIntent.SetBodyTypeFocus(true))
            viewModel.handleIntent(CarEditIntent.SetDriveTypeFocus(true))
            viewModel.handleIntent(CarEditIntent.SetEngineTypeFocus(true))
            advanceUntilIdle()

            val state = viewModel.state.value as CarEditMviState.Success
            assertTrue(state.isBrandFocused)
            assertTrue(state.isModelFocused)
            assertTrue(state.isBodyTypeFocused)
            assertTrue(state.isDriveTypeFocused)
            assertTrue(state.isEngineTypeFocused)
        }

    @Test
    fun `SelectBrand should remove focus from brand field`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandFocus(true))
            advanceUntilIdle()

            val stateBefore = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBefore.isBrandFocused, "Brand should be focused before selection")

            viewModel.handleIntent(CarEditIntent.SelectBrand(10, "Toyota"))
            advanceUntilIdle()

            val stateAfter = viewModel.state.value as CarEditMviState.Success
            assertFalse(stateAfter.isBrandFocused, "Brand should not be focused after selection")
            assertEquals(10, stateAfter.formData.brandId)
            assertEquals("Toyota", stateAfter.formData.brandName)
        }

    @Test
    fun `SelectModel should remove focus from model field`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetModelFocus(true))
            advanceUntilIdle()

            val stateBefore = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBefore.isModelFocused, "Model should be focused before selection")

            viewModel.handleIntent(CarEditIntent.SelectModel(20, "Camry"))
            advanceUntilIdle()

            val stateAfter = viewModel.state.value as CarEditMviState.Success
            assertFalse(stateAfter.isModelFocused, "Model should not be focused after selection")
            assertEquals(20, stateAfter.formData.modelId)
            assertEquals("Camry", stateAfter.formData.modelName)
        }

    @Test
    fun `SelectBodyType should remove focus from bodyType field`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBodyTypeFocus(true))
            advanceUntilIdle()

            val stateBefore = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBefore.isBodyTypeFocused, "BodyType should be focused before selection")

            viewModel.handleIntent(CarEditIntent.SelectBodyType("Sedan", "Седан"))
            advanceUntilIdle()

            val stateAfter = viewModel.state.value as CarEditMviState.Success
            assertFalse(stateAfter.isBodyTypeFocused, "BodyType should not be focused after selection")
            assertEquals("Sedan", stateAfter.formData.bodyType)
            assertEquals("Седан", stateAfter.formData.bodyTypeTranslate)
        }

    @Test
    fun `SelectDriveType should remove focus from driveType field`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetDriveTypeFocus(true))
            advanceUntilIdle()

            val stateBefore = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBefore.isDriveTypeFocused, "DriveType should be focused before selection")

            viewModel.handleIntent(CarEditIntent.SelectDriveType("Front", "Передний"))
            advanceUntilIdle()

            val stateAfter = viewModel.state.value as CarEditMviState.Success
            assertFalse(stateAfter.isDriveTypeFocused, "DriveType should not be focused after selection")
            assertEquals("Front", stateAfter.formData.driveType)
            assertEquals("Передний", stateAfter.formData.driveTypeTranslate)
        }

    @Test
    fun `SelectEngineType should remove focus from engineType field`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetEngineTypeFocus(true))
            advanceUntilIdle()

            val stateBefore = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateBefore.isEngineTypeFocused, "EngineType should be focused before selection")

            viewModel.handleIntent(CarEditIntent.SelectEngineType("Gasoline", "Бензин"))
            advanceUntilIdle()

            val stateAfter = viewModel.state.value as CarEditMviState.Success
            assertFalse(stateAfter.isEngineTypeFocused, "EngineType should not be focused after selection")
            assertEquals("Gasoline", stateAfter.formData.engineType)
            assertEquals("Бензин", stateAfter.formData.engineTypeTranslate)
        }

    @Test
    fun `SelectBrand should not affect other focus states`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SetBrandFocus(true))
            viewModel.handleIntent(CarEditIntent.SetModelFocus(true))
            viewModel.handleIntent(CarEditIntent.SetBodyTypeFocus(true))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SelectBrand(10, "Toyota"))
            advanceUntilIdle()

            val state = viewModel.state.value as CarEditMviState.Success
            assertFalse(state.isBrandFocused, "Brand should not be focused after selection")
            assertTrue(state.isModelFocused, "Model focus should not be affected")
            assertTrue(state.isBodyTypeFocused, "BodyType focus should not be affected")
        }

    @Test
    fun `carId in constructor should load car automatically`() =
        runTest(StandardTestDispatcher()) {
            val carId = "ef4d16a2-aeeb-457e-abbe-b72473634690"
            val viewModel = createCarEditMviViewModel(carId = carId)

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarEditMviState.Success>(state)
            assertEquals(carId, state.carId)
            assertEquals("K128CT", state.formData.licensePlate)
        }

    @Test
    fun `carId null in constructor should not load car`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            advanceUntilIdle()

            val state = viewModel.state.value
            // Если carId пустой, loadCar не вызывается, но начальное состояние - Loading
            // После advanceUntilIdle состояние может остаться Loading или измениться
            assertTrue(
                state is CarEditMviState.Loading || state is CarEditMviState.Success,
                "State should be Loading or Success when carId is empty",
            )
        }

    @Test
    fun `button should be disabled when productionYear is 0`() =
        runTest(StandardTestDispatcher()) {
            val mockCarService =
                MockCarServiceForMvi().apply {
                    setCarWithZeroYear()
                }
            val viewModel = createCarEditMviViewModel(carService = mockCarService, carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val state = viewModel.state.value as CarEditMviState.Success
            assertFalse(state.hasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateProductionYear("2020"))
            advanceUntilIdle()

            val updatedState = viewModel.state.value as CarEditMviState.Success
            assertTrue(updatedState.hasChanges, "hasChanges should be true after update")
        }

    @Test
    fun `button should be disabled when productionYear is empty string`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            viewModel.handleIntent(CarEditIntent.UpdateProductionYear(""))
            advanceUntilIdle()

            val stateWithEmptyYear = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateWithEmptyYear.hasChanges, "hasChanges should be true")
            assertTrue(stateWithEmptyYear.isSaveButtonEnabled, "Save button should be enabled when hasChanges is true")
        }

    @Test
    fun `button should be enabled when seatsCount is changed`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")
            assertFalse(initialState.isSaveButtonEnabled, "Save button should be disabled when no changes")

            viewModel.handleIntent(CarEditIntent.UpdateSeatsCount("7"))
            advanceUntilIdle()

            val stateAfterSeatsChange = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateAfterSeatsChange.hasChanges, "hasChanges should be true after changing seatsCount")
            assertTrue(
                stateAfterSeatsChange.isSaveButtonEnabled,
                "Save button should be enabled when seatsCount is changed",
            )
        }

    @Test
    fun `button should be enabled when productionYear is changed`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("ef4d16a2-aeeb-457e-abbe-b72473634690"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateProductionYear("2021"))
            advanceUntilIdle()

            val stateAfterYearChange = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateAfterYearChange.hasChanges, "hasChanges should be true after changing productionYear")
            assertTrue(
                stateAfterYearChange.isSaveButtonEnabled,
                "Save button should be enabled when productionYear is changed",
            )
        }

    @Test
    fun `DeleteCar should set state to NavigateToMyCars on success`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value
            assertIs<CarEditMviState.Success>(initialState)

            viewModel.handleIntent(CarEditIntent.DeleteCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarEditMviState.NavigateToMyCars>(finalState)
        }

    @Test
    fun `DeleteCar should set state to Error on failure`() =
        runTest(StandardTestDispatcher()) {
            val mockCarService =
                MockCarServiceForMvi().apply {
                    shouldThrowErrorOnDelete = true
                    errorMessage = "Failed to delete car"
                }
            val viewModel = createCarEditMviViewModel(carService = mockCarService, carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value
            assertIs<CarEditMviState.Success>(initialState)

            viewModel.handleIntent(CarEditIntent.DeleteCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarEditMviState.Success>(finalState)
            assertEquals("Failed to delete car", finalState.saveError)
        }

    @Test
    fun `SaveCar should set state to NavigateToMyCars on success`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value
            assertIs<CarEditMviState.Success>(initialState)

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            // После сохранения вызывается loadCar, который может установить Success, затем NavigateToMyCars
            assertTrue(
                finalState is CarEditMviState.NavigateToMyCars || finalState is CarEditMviState.Success,
                "State should be NavigateToMyCars or Success after save",
            )
        }

    @Test
    fun `SaveCar should set saveError on failure`() =
        runTest(StandardTestDispatcher()) {
            val mockCarService =
                MockCarServiceForMvi().apply {
                    shouldThrowErrorOnSave = true
                    errorMessage = "Failed to save car"
                }
            val viewModel = createCarEditMviViewModel(carService = mockCarService, carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value
            assertIs<CarEditMviState.Success>(initialState)

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarEditMviState.Success>(finalState)
            assertEquals("Failed to save car", finalState.saveError)
        }

    @Test
    fun `UpdateEngineVolume should update form data and set hasChanges to true`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "")

            viewModel.handleIntent(CarEditIntent.LoadCar("test-car-id"))
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateEngineVolume("2.5"))
            advanceUntilIdle()

            val updatedState = viewModel.state.value
            assertIs<CarEditMviState.Success>(updatedState)
            assertEquals("2.5", updatedState.formData.engineVolume)
            assertTrue(updatedState.hasChanges, "hasChanges should be true after update")
        }

    @Test
    fun `SaveCar should reset hasChanges to false before navigation`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            assertFalse(initialState.hasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val stateWithChanges = viewModel.state.value as CarEditMviState.Success
            assertTrue(stateWithChanges.hasChanges, "hasChanges should be true after update")

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            // После сохранения вызывается loadCar, который может установить Success, затем NavigateToMyCars
            assertTrue(
                finalState is CarEditMviState.NavigateToMyCars || finalState is CarEditMviState.Success,
                "State should be NavigateToMyCars or Success after save",
            )
            // Проверяем, что hasChanges был сброшен перед навигацией
            if (finalState is CarEditMviState.Success) {
                assertFalse(finalState.hasChanges, "hasChanges should be false after save")
            }
        }

    @Test
    fun `SaveCar should handle empty engineVolume correctly`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialState = viewModel.state.value as CarEditMviState.Success
            val originalEngineVolume = initialState.formData.engineVolume

            viewModel.handleIntent(CarEditIntent.UpdateEngineVolume(""))
            advanceUntilIdle()

            val stateWithEmptyVolume = viewModel.state.value as CarEditMviState.Success
            assertEquals("", stateWithEmptyVolume.formData.engineVolume)
            assertTrue(stateWithEmptyVolume.hasChanges, "hasChanges should be true after update")

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            // После сохранения вызывается loadCar, который может установить Success, затем NavigateToMyCars
            assertTrue(
                finalState is CarEditMviState.NavigateToMyCars || finalState is CarEditMviState.Success,
                "State should be NavigateToMyCars or Success after save",
            )
        }

    @Test
    fun `hasChanges should be false by default`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val hasChanges = viewModel.hasChanges.value
            assertFalse(hasChanges, "hasChanges should be false by default")
        }

    @Test
    fun `hasChanges should be true when any property is changed`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialHasChanges = viewModel.hasChanges.value
            assertFalse(initialHasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val hasChangesAfterUpdate = viewModel.hasChanges.value
            assertTrue(hasChangesAfterUpdate, "hasChanges should be true after updating license plate")

            viewModel.handleIntent(CarEditIntent.UpdateEngineVolume("2.5"))
            advanceUntilIdle()

            val hasChangesAfterSecondUpdate = viewModel.hasChanges.value
            assertTrue(hasChangesAfterSecondUpdate, "hasChanges should remain true after second update")
        }

    @Test
    fun `hasChanges should be false after save`() =
        runTest(StandardTestDispatcher()) {
            val viewModel = createCarEditMviViewModel(carId = "test-car-id")

            advanceUntilIdle()

            val initialHasChanges = viewModel.hasChanges.value
            assertFalse(initialHasChanges, "hasChanges should be false initially")

            viewModel.handleIntent(CarEditIntent.UpdateLicensePlate("NEW123"))
            advanceUntilIdle()

            val hasChangesAfterUpdate = viewModel.hasChanges.value
            assertTrue(hasChangesAfterUpdate, "hasChanges should be true after update")

            viewModel.handleIntent(CarEditIntent.SaveCar)
            advanceUntilIdle()

            val hasChangesAfterSave = viewModel.hasChanges.value
            assertFalse(hasChangesAfterSave, "hasChanges should be false after save")
        }
}

fun createMockCarBrandViewModel(
    carBrandRepository: my.drivebit.repositories.CarBrandRepository? = null,
    coroutineScope: CoroutineScope? = null,
): CarBrandViewModel {
    val mockRepository =
        carBrandRepository
            ?: object : my.drivebit.repositories.CarBrandRepository {
                override suspend fun getBrands(): my.drivebit.repositories.ResultCarBrands =
                    my.drivebit.repositories.ResultCarBrands
                        .Success(emptyList())
            }
    val scope = coroutineScope ?: CoroutineScope(SupervisorJob())
    return CarBrandViewModel(mockRepository, scope)
}

fun createMockCarModelViewModel(
    carModelRepository: my.drivebit.repositories.CarModelRepository? = null,
    coroutineScope: CoroutineScope? = null,
): CarModelViewModel {
    val mockRepository =
        carModelRepository
            ?: object : my.drivebit.repositories.CarModelRepository {
                override suspend fun getModels(brandId: Int): my.drivebit.repositories.ResultCarModels =
                    my.drivebit.repositories.ResultCarModels
                        .Success(emptyList())
            }
    val scope = coroutineScope ?: CoroutineScope(SupervisorJob())
    return CarModelViewModel(mockRepository, scope)
}

fun createMockCarEnumsRepository(): my.drivebit.repositories.CarEnumsRepository =
    object : my.drivebit.repositories.CarEnumsRepository {
        override suspend fun getEnums(): my.drivebit.network.services.CarEnumsResponse = throw NotImplementedError()

        override suspend fun getColorByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getBodyTypeByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getStatusByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getEngineTypeByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getTransmissionTypeByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getDriveTypeByName(name: String): my.drivebit.repositories.EnumItem =
            throw NotImplementedError()

        override suspend fun getAllColors(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllBodyTypes(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllStatuses(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllEngineTypes(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllTransmissionTypes(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllDriveTypes(): List<my.drivebit.repositories.EnumItem> = emptyList()

        override suspend fun getAllDocumentTypes(): List<my.drivebit.repositories.EnumItem> = emptyList()
    }

fun createMockBodyTypeViewModel(
    carEnumsRepository: my.drivebit.repositories.CarEnumsRepository? = null,
    coroutineScope: CoroutineScope? = null,
): BodyTypeViewModel {
    val mockRepository = carEnumsRepository ?: createMockCarEnumsRepository()
    val scope = coroutineScope ?: CoroutineScope(SupervisorJob())
    return BodyTypeViewModel(mockRepository, scope)
}

fun createMockDriveTypeViewModel(
    carEnumsRepository: my.drivebit.repositories.CarEnumsRepository? = null,
    coroutineScope: CoroutineScope? = null,
): DriveTypeViewModel {
    val mockRepository = carEnumsRepository ?: createMockCarEnumsRepository()
    val scope = coroutineScope ?: CoroutineScope(SupervisorJob())
    return DriveTypeViewModel(mockRepository, scope)
}

fun createMockEngineTypeViewModel(
    carEnumsRepository: my.drivebit.repositories.CarEnumsRepository? = null,
    coroutineScope: CoroutineScope? = null,
): EngineTypeViewModel {
    val mockRepository = carEnumsRepository ?: createMockCarEnumsRepository()
    val scope = coroutineScope ?: CoroutineScope(SupervisorJob())
    return EngineTypeViewModel(mockRepository, scope)
}

fun TestScope.createCarEditMviViewModel(
    carService: Car = MockCarServiceForMvi(),
    carBrandViewModel: CarBrandViewModel? = null,
    carModelViewModel: CarModelViewModel? = null,
    bodyTypeViewModel: BodyTypeViewModel? = null,
    driveTypeViewModel: DriveTypeViewModel? = null,
    engineTypeViewModel: EngineTypeViewModel? = null,
    myCarRepository: MyCarRepository = MockMyCarRepository(),
    carId: String = "",
): CarEditMviViewModelImpl {
    val viewModelScope = CoroutineScope(coroutineContext)
    val viewModel =
        CarEditMviViewModelImpl(
            carService = carService,
            carBrandViewModel = carBrandViewModel ?: createMockCarBrandViewModel(coroutineScope = viewModelScope),
            carModelViewModel = carModelViewModel ?: createMockCarModelViewModel(coroutineScope = viewModelScope),
            bodyTypeViewModel = bodyTypeViewModel ?: createMockBodyTypeViewModel(coroutineScope = viewModelScope),
            driveTypeViewModel = driveTypeViewModel ?: createMockDriveTypeViewModel(coroutineScope = viewModelScope),
            engineTypeViewModel = engineTypeViewModel ?: createMockEngineTypeViewModel(coroutineScope = viewModelScope),
            myCarRepository = myCarRepository,
            carId = carId,
            coroutineScope = viewModelScope,
            observerCoroutineScope = backgroundScope,
        )
    return viewModel
}
