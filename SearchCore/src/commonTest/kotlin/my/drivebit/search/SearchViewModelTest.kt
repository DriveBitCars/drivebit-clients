package my.drivebit.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
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
}
