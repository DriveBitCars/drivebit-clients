package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestData(
    val id: String,
    val value: String,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CachedRepositoryTest {
    @Test
    fun `should return data from storage if cached`() =
        runTest(StandardTestDispatcher()) {
            val settings = InMemorySettings()
            val cachedData = TestData(id = "1", value = "cached")
            settings.putString("test_key", """{"id":"1","value":"cached"}""")

            var callCount = 0
            val service: suspend () -> TestData = {
                callCount++
                TestData(id = "2", value = "network")
            }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            var result: TestData? = null
            repository.get().collect {
                result = it
                return@collect
            }
            advanceUntilIdle()

            assertEquals(0, callCount)
            assertEquals(cachedData, result)
        }

    @Test
    fun `should fetch from network if storage is empty`() =
        runTest {
            val settings = InMemorySettings()
            val networkData = TestData(id = "2", value = "network")

            var callCount = 0
            val service: suspend () -> TestData = {
                callCount++
                networkData
            }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            val result = repository.get().first()

            assertEquals(1, callCount)
            assertEquals(networkData, result)
            val cached = settings.getString("test_key", "")
            assertEquals("""{"id":"2","value":"network"}""", cached)
        }

    @Test
    fun `should cache data after fetching from network`() =
        runTest {
            val settings = InMemorySettings()
            val networkData = TestData(id = "3", value = "network")

            val service: suspend () -> TestData = { networkData }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            repository.get().first()
            val cached = settings.getString("test_key", "")
            assertEquals("""{"id":"3","value":"network"}""", cached)
        }

    @Test
    fun `should clear cache`() =
        runTest {
            val settings = InMemorySettings()
            settings.putString("test_key", """{"id":"1","value":"cached"}""")

            val service: suspend () -> TestData = { TestData(id = "2", value = "network") }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            repository.clearCache()
            val cached = settings.getString("test_key", "")
            assertEquals("", cached)
        }

    @Test
    fun `should fetch from network after cache cleared`() =
        runTest {
            val settings = InMemorySettings()
            settings.putString("test_key", """{"id":"1","value":"cached"}""")

            var callCount = 0
            val networkData = TestData(id = "2", value = "network")
            val service: suspend () -> TestData = {
                callCount++
                networkData
            }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            repository.clearCache()
            val result = repository.get().first()

            assertEquals(1, callCount)
            assertEquals(networkData, result)
        }

    @Test
    fun `should use cached data on subsequent calls`() =
        runTest(StandardTestDispatcher()) {
            val settings = InMemorySettings()

            var callCount = 0
            val networkData = TestData(id = "1", value = "network")
            val service: suspend () -> TestData = {
                callCount++
                networkData
            }

            val json = Json { ignoreUnknownKeys = true }
            val repository =
                CachedRepositoryImpl(
                    key = "test_key",
                    serializer = TestData.serializer(),
                    service = service,
                    settings = settings,
                    json = json,
                )

            var firstResult: TestData? = null
            repository.get().collect {
                firstResult = it
                return@collect
            }
            advanceUntilIdle()
            var secondResult: TestData? = null
            repository.get().collect {
                secondResult = it
                return@collect
            }
            advanceUntilIdle()
            var thirdResult: TestData? = null
            repository.get().collect {
                thirdResult = it
                return@collect
            }
            advanceUntilIdle()

            assertEquals(1, callCount)
            assertEquals(networkData, firstResult)
            assertEquals(networkData, secondResult)
            assertEquals(networkData, thirdResult)
        }
}
