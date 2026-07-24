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
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MainContentViewModelTest {
    private fun createViewModel(
        repository: MockCarSearchRepository,
        dispatcher: CoroutineDispatcher,
        onNavigatePage: ((Int) -> Unit)? = null,
    ): MainContentViewModelImpl =
        MainContentViewModelImpl(
            carSearchRepository = repository,
            onNavigatePage = onNavigatePage,
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
        )

    private fun repositoryWithCars(
        pageIndex: Int = 0,
        totalPages: Int = 1,
        totalCount: Int = 1,
    ): MockCarSearchRepository =
        MockCarSearchRepository().apply {
            this.pageIndex = pageIndex
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
                    totalCount = totalCount,
                    totalPages = totalPages,
                )
        }

    @Test
    fun `firstList emits FirstList after repository returns data`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                createViewModel(
                    repositoryWithCars(totalPages = 3, totalCount = 19),
                    dispatcher,
                )

            assertIs<MainContentListState.Loading>(viewModel.firstList.value)

            advanceUntilIdle()

            val state = assertIs<MainContentListState.FirstList>(viewModel.firstList.value)
            assertEquals(19, state.totalCount)
            assertEquals(3, state.totalPages)
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

    @Test
    fun `paginationInfo uses page from repository after FirstList`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel =
                createViewModel(
                    repositoryWithCars(pageIndex = 1, totalPages = 5, totalCount = 42),
                    dispatcher,
                )

            advanceUntilIdle()

            assertEquals(Triple(1, 5, 42), viewModel.paginationInfo.value)
            assertEquals(1, viewModel.displayedCars.value.size)
            assertEquals(
                "car-1",
                viewModel.displayedCars.value
                    .first()
                    .id,
            )
        }

    @Test
    fun `setPage invokes onNavigatePage callback`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var navigatedTo: Int? = null
            val viewModel =
                createViewModel(
                    repositoryWithCars(),
                    dispatcher,
                    onNavigatePage = { navigatedTo = it },
                )

            advanceUntilIdle()
            viewModel.setPage(2)

            assertEquals(2, navigatedTo)
            assertIs<MainContentListState.Loading>(viewModel.firstList.value)
        }
}
