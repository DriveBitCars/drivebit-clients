package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineDispatcher
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
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MainContentViewModelTest {
    private fun createViewModel(
        repository: MockCarSearchRepository,
        dispatcher: CoroutineDispatcher,
    ): MainContentViewModelImpl =
        MainContentViewModelImpl(
            carSearchRepository = repository,
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
        )

    private fun repositoryWithCars(): MockCarSearchRepository =
        MockCarSearchRepository().apply {
            searchResult =
                CarSearchResponse(
                    cars =
                        listOf(
                            CarItem(
                                id = "car-1",
                                general =
                                    CarGeneral(
                                        brandName = "BMW",
                                        modelName = "X5",
                                        vin = "VIN123",
                                        seats = 4,
                                        address =
                                            CarAddress(
                                                geoLat = 0.0,
                                                geoLon = 0.0,
                                            ),
                                    ),
                            ),
                        ),
                    totalCount = 1,
                    totalPages = 1,
                )
        }

    @Test
    fun `firstList emits FirstList after repository returns data`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel = createViewModel(repositoryWithCars(), dispatcher)

            assertIs<MainContentListState.Loading>(viewModel.firstList.value)

            advanceUntilIdle()

            assertIs<MainContentListState.FirstList>(viewModel.firstList.value)
        }

    @Test
    fun `markSearchStarted sets Loading`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel = createViewModel(repositoryWithCars(), dispatcher)

            advanceUntilIdle()
            assertIs<MainContentListState.FirstList>(viewModel.firstList.value)

            viewModel.markSearchStarted()
            assertIs<MainContentListState.Loading>(viewModel.firstList.value)
        }

    @Test
    fun `repository error sets Error state`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val repository = repositoryWithCars()
            repository.shouldThrowError = true
            val viewModel = createViewModel(repository, dispatcher)

            advanceUntilIdle()

            assertIs<MainContentListState.Error>(viewModel.firstList.value)
        }
}
