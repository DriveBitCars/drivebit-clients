package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.repositories.CarSearchRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockCarSearchRepository : CarSearchRepository {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var searchResult: CarSearchResponse = CarSearchResponse(emptyList())

    override val searchCarsByUserCity: kotlinx.coroutines.flow.Flow<CarSearchResponse>
        get() {
            if (shouldThrowError) {
                return kotlinx.coroutines.flow.flow {
                    throw Exception(errorMessage)
                }
            }
            return kotlinx.coroutines.flow.flowOf(searchResult)
        }

    override val currentPage: kotlinx.coroutines.flow.Flow<Int> =
        kotlinx.coroutines.flow.flowOf(0)

    override fun setPage(page: Int) {}

    override fun refreshSearch() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarSearchViewModelTest {
    @Test
    fun `initial state should be Idle`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            assertIs<CarSearchState.Idle>(viewModel.state.value)
        }

    @Test
    fun `SearchCars intent should set state to Success with cars`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            val expectedCars =
                listOf(
                    testCarItem(id = "1", brandName = "BMW", modelName = "X5"),
                    testCarItem(id = "2", brandName = "Audi", modelName = "A4"),
                )
            mockRepository.searchResult = CarSearchResponse(expectedCars)
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(CarSearchIntent.SearchCars)
            testDispatcher.scheduler.advanceUntilIdle()

            assertIs<CarSearchState.Success>(viewModel.state.value)
            val successState = viewModel.state.value as CarSearchState.Success
            assertEquals(2, successState.cars.size)
            assertEquals("BMW", successState.cars[0].general.brandName)
            assertEquals("Audi", successState.cars[1].general.brandName)
        }

    @Test
    fun `SearchCars intent should set state to Error on exception`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            mockRepository.shouldThrowError = true
            mockRepository.errorMessage = "Network error"
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(CarSearchIntent.SearchCars)
            testDispatcher.scheduler.advanceUntilIdle()

            assertIs<CarSearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as CarSearchState.Error
            assertEquals("Network error", errorState.message)
        }

    @Test
    fun `SearchCarsWithDates intent should trigger search and set state to Success`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            val expectedCars =
                listOf(
                    testCarItem(id = "1", brandName = "BMW", modelName = "X5"),
                    testCarItem(id = "2", brandName = "Audi", modelName = "A4"),
                )
            mockRepository.searchResult = CarSearchResponse(expectedCars)
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(
                CarSearchIntent.SearchCarsWithDates(
                    dateFrom = "2024-01-01",
                    dateTo = "2024-01-10",
                ),
            )
            testDispatcher.scheduler.advanceUntilIdle()

            assertIs<CarSearchState.Success>(viewModel.state.value)
            val successState = viewModel.state.value as CarSearchState.Success
            assertEquals(2, successState.cars.size)
        }

    @Test
    fun `SearchCars intent should transition from Idle to Success`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            assertIs<CarSearchState.Idle>(viewModel.state.value)

            viewModel.handleIntent(CarSearchIntent.SearchCars)
            testDispatcher.scheduler.advanceUntilIdle()

            assertIs<CarSearchState.Success>(viewModel.state.value)
        }

    @Test
    fun `SearchCars intent should set state to Error with default message on exception with empty message`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockRepository = MockCarSearchRepository()
            mockRepository.shouldThrowError = true
            mockRepository.errorMessage = ""
            val viewModel =
                CarSearchViewModelImpl(
                    carSearchRepository = mockRepository,
                    coroutineScope = testScope,
                )

            viewModel.handleIntent(CarSearchIntent.SearchCars)
            testDispatcher.scheduler.advanceUntilIdle()

            assertIs<CarSearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as CarSearchState.Error
            assertEquals("Не удалось найти машины", errorState.message)
        }
}

private fun testCarItem(
    id: String,
    brandName: String,
    modelName: String,
): CarItem =
    CarItem(
        id = id,
        general =
            CarGeneral(
                brandName = brandName,
                modelName = modelName,
                vin = "VIN123",
                seats = 4,
                address =
                    CarAddress(
                        geoLat = 0.0,
                        geoLon = 0.0,
                    ),
            ),
    )
