package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.City
import my.drivebit.repositories.CityRepository
import my.drivebit.repositories.ResultCities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeCityRepository : CityRepository {
    var shouldReturnError = false
    var errorMessage: String = "Error"
    var lastQuery: String? = null
    var callCount = 0
    var getAllCallCount = 0

    override suspend fun searchCities(query: String): ResultCities {
        lastQuery = query
        callCount++
        if (shouldReturnError) {
            return ResultCities.Error(errorMessage)
        }
        return ResultCities.Success(
            listOf(
                City(id = 1, name = "Москва"),
                City(id = 2, name = "Московская область"),
            ),
        )
    }

    override suspend fun getAllCities(): ResultCities {
        getAllCallCount++
        if (shouldReturnError) {
            return ResultCities.Error(errorMessage)
        }
        return ResultCities.Success(
            listOf(
                City(id = 10, name = "Казань", regionName = "Татарстан"),
                City(id = 11, name = "Калининград", regionName = "Калининградская область"),
            ),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {
    @Test
    fun `initial state should have empty query and cities`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                )

            assertEquals("", viewModel.query.value)
            assertTrue(viewModel.cities.value.isEmpty())
            assertNull(viewModel.error.value)
        }

    @Test
    fun `updateQuery should update query value`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                )

            viewModel.updateQuery("Москва")

            assertEquals("Москва", viewModel.query.value)
        }

    @Test
    fun `updateQuery with non-empty query should trigger search after debounce`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Москва", fakeRepo.lastQuery)
            assertEquals(1, fakeRepo.callCount)
        }

    @Test
    fun `updateQuery with empty query should cancel search and clear cities`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.cities.value.isNotEmpty())

            viewModel.updateQuery("")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.cities.value.isEmpty())
            assertNull(viewModel.error.value)
        }

    @Test
    fun `should cancel previous search when new query is entered`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(50)
            viewModel.updateQuery("Санкт-Петербург")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Санкт-Петербург", fakeRepo.lastQuery)
            assertEquals(1, fakeRepo.callCount)
        }

    @Test
    fun `should update cities on successful search`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(2, viewModel.cities.value.size)
            assertEquals("Москва", viewModel.cities.value[0].name)
            assertEquals("Московская область", viewModel.cities.value[1].name)
            assertNull(viewModel.error.value)
        }

    @Test
    fun `should update error on failed search`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo =
                FakeCityRepository().apply {
                    shouldReturnError = true
                    errorMessage = "Network error"
                }
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Network error", viewModel.error.value)
            assertTrue(viewModel.cities.value.isEmpty())
        }

    @Test
    fun `should not update cities if query changed during search`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(50)
            viewModel.updateQuery("Санкт")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Санкт", viewModel.query.value)
        }

    @Test
    fun `clearQuery should clear query cities and error`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.updateQuery("Москва")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.cities.value.isNotEmpty())

            viewModel.clearQuery()

            assertEquals("", viewModel.query.value)
            assertTrue(viewModel.cities.value.isEmpty())
            assertNull(viewModel.error.value)
        }

    @Test
    fun `browse all mode loads catalog and filters locally by query`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.enableBrowseAllCitiesMode()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, fakeRepo.getAllCallCount)
            assertEquals(2, viewModel.cities.value.size)
            assertEquals("Казань", viewModel.cities.value[0].name)
            assertEquals("Калининград", viewModel.cities.value[1].name)

            viewModel.updateQuery("Калинин")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, viewModel.cities.value.size)
            assertEquals("Калининград", viewModel.cities.value[0].name)
            assertEquals(0, fakeRepo.callCount)
        }

    @Test
    fun `clearQuery in browse all mode restores full catalog`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
            val fakeRepo = FakeCityRepository()
            val viewModel =
                CityViewModel(
                    cityRepository = fakeRepo,
                    coroutineScope = testScope,
                    debounceTimeMs = 100,
                )

            viewModel.enableBrowseAllCitiesMode()
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.updateQuery("Казань")
            testDispatcher.scheduler.advanceTimeBy(100)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(1, viewModel.cities.value.size)

            viewModel.clearQuery()

            assertEquals(2, viewModel.cities.value.size)
        }
}
