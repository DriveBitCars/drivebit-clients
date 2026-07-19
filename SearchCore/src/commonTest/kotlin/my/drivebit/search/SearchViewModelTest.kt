package my.drivebit.search

import io.ktor.http.HttpStatusCode
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
import my.drivebit.network.NetworkException
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
                                        cars = listOf(testCarItem(id = "1", brand = "BMW", model = "")),
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
    fun `load url hides technical ktor body transformation errors`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val technical =
                "Expected response body of the type 'class CarDTOPagedResult' but was " +
                    "'class SourceByteReadChannel' In response from " +
                    "`https://drivebit.ru/api/Car/list/filtered/158835?page=1&pageSize=9&BrandId=1` " +
                    "Response status `503` Response header `ContentType: null`"
            val vm =
                SearchViewModel(
                    repositoryFactory = {
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flow { throw IllegalStateException(technical) }
                        }
                    },
                    brandResolver = { null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/search/abarth")
            advanceUntilIdle()

            val error = assertIs<SearchUiState.Error>(vm.state.value)
            assertEquals("Сервер временно недоступен. Попробуйте позже", error.message)
        }

    @Test
    fun `load url maps NetworkException 503 to friendly message`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val vm =
                SearchViewModel(
                    repositoryFactory = {
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flow {
                                    throw NetworkException(
                                        HttpStatusCode.ServiceUnavailable,
                                        "",
                                    )
                                }
                        }
                    },
                    brandResolver = { null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/search/abarth")
            advanceUntilIdle()

            val error = assertIs<SearchUiState.Error>(vm.state.value)
            assertEquals("Сервер временно недоступен. Попробуйте позже", error.message)
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
                                            cars = listOf(testCarItem(id = page.toString(), brand = "Page", model = page.toString())),
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

    @Test
    fun `brand then body type load completes with both filters`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            var lastFilters: SearchFilterSet? = null
            val vm =
                SearchViewModel(
                    repositoryFactory = { filters ->
                        lastFilters = filters
                        object : SearchCarRepository {
                            override val results: Flow<SearchCarsResult> =
                                flowOf(SearchCarsResult(totalCount = 0, totalPages = 0))
                        }
                    },
                    brandResolver = { slug -> if (slug == "bmw") 10 to "BMW" else null },
                    modelResolver = { _, _ -> null },
                    coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
                )

            vm.load("/bmw")
            advanceUntilIdle()
            vm.load("/bmw?bodyType=SUV&bodyTypeLabel=%D0%92%D0%BD%D0%B5%D0%B4%D0%BE%D1%80%D0%BE%D0%B6%D0%BD%D0%B8%D0%BA")
            advanceUntilIdle()

            val results = assertIs<SearchUiState.Results>(vm.state.value)
            assertEquals(10, results.filters.brandId)
            assertEquals("BMW", results.filters.brandName)
            assertEquals("SUV", results.filters.bodyType)
            assertEquals("Внедорожник", results.filters.bodyTypeLabel)
            assertEquals("SUV", lastFilters?.bodyType)
            assertEquals(10, lastFilters?.brandId)
        }
}
