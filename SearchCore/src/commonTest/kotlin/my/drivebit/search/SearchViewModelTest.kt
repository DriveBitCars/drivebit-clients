package my.drivebit.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @Test
    fun `load url emits results from repository flow`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val vm =
                SearchViewModel(
                    repositoryFactory = { filters ->
                        assertEquals(10, filters.brandId)
                        assertEquals(5, filters.seatsMin)
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flowOf(
                                    SearchCarsResult(
                                        cars = listOf(SearchCarCard(id = "1", title = "BMW")),
                                        totalCount = 1,
                                        totalPages = 1,
                                    ),
                                )
                        }
                    },
                    brandResolver = { slug -> if (slug == "bmw") 10 to "BMW" else null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            assertIs<SearchUiState.Loading>(vm.state.value)
            vm.load("/search/bmw?seatsMin=5")
            advanceUntilIdle()

            val results = assertIs<SearchUiState.Results>(vm.state.value)
            assertEquals(10, results.filters.brandId)
            assertEquals("BMW", results.filters.brandName)
            assertEquals(5, results.filters.seatsMin)
            assertEquals(1, results.result.cars.size)
        }

    @Test
    fun `load url emits error when repository flow fails`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val vm =
                SearchViewModel(
                    repositoryFactory = {
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flow { throw IllegalStateException("boom") }
                        }
                    },
                    brandResolver = { null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/moskva/search")
            advanceUntilIdle()

            val error = assertIs<SearchUiState.Error>(vm.state.value)
            assertEquals("boom", error.message)
        }

    @Test
    fun `load url with page 2 passes page to repository factory`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var capturedPage: Int? = null
            val vm =
                SearchViewModel(
                    repositoryFactory = { filters ->
                        capturedPage = filters.page
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flowOf(SearchCarsResult(totalCount = 0, totalPages = 3))
                        }
                    },
                    brandResolver = { null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/moskva/search?page=2")
            advanceUntilIdle()

            assertEquals(2, capturedPage)
            val results = assertIs<SearchUiState.Results>(vm.state.value)
            assertEquals(2, results.filters.page)
        }

    @Test
    fun `later load wins over slower in-flight load`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val vm =
                SearchViewModel(
                    repositoryFactory = { filters ->
                        val page = filters.page
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flow {
                                    if (page == 1) {
                                        delay(100)
                                    }
                                    emit(
                                        SearchCarsResult(
                                            cars = listOf(SearchCarCard(id = page.toString(), title = "Page $page")),
                                            totalCount = 1,
                                            totalPages = 3,
                                        ),
                                    )
                                }
                        }
                    },
                    brandResolver = { null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/moskva/search?page=1")
            vm.load("/moskva/search?page=2")
            advanceUntilIdle()

            val results = assertIs<SearchUiState.Results>(vm.state.value)
            assertEquals(2, results.filters.page)
            assertEquals("2", results.result.cars.first().id)
        }
}
