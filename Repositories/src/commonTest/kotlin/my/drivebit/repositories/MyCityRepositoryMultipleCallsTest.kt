package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockDictionaryWithCallTracking : Dictionary {
    var searchCitiesCallCount = 0
    var lastSearchQuery: String? = null
    var searchResults: Map<String, List<City>> =
        mapOf(
            "Москва" to listOf(City(id = 158830, name = "Москва")),
            "Росалина" to listOf(City(id = 123, name = "Росалина")),
        )

    override suspend fun getCarBrands(): List<my.drivebit.network.services.CarBrand> = throw NotImplementedError()

    override suspend fun getCarModels(brandId: Int): List<my.drivebit.network.services.CarModel> =
        throw NotImplementedError()

    override suspend fun getAllCities(): List<City> = throw NotImplementedError()

    override suspend fun getCarEnums(): my.drivebit.network.services.CarEnumsResponse = throw NotImplementedError()

    override suspend fun getDocumentEnums(): my.drivebit.network.services.DocumentEnumsResponse =
        throw NotImplementedError()

    override suspend fun getFiltersSuggested(): List<my.drivebit.network.services.FilterSuggestion> = emptyList()

    override suspend fun searchCities(query: String): List<City> {
        searchCitiesCallCount++
        lastSearchQuery = query
        println("🔍 [MockDictionary] searchCities() вызван #$searchCitiesCallCount с запросом: '$query'")
        return searchResults[query] ?: emptyList()
    }
}

class MockStorageWithCallTracking : Storage {
    private var storedData = mutableMapOf<String, String>()

    override fun getString(
        key: String,
        defaultValue: String,
    ): String {
        println("💾 [MockStorage] getString('$key') = '${storedData[key] ?: defaultValue}'")
        return storedData[key] ?: defaultValue
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        println("💾 [MockStorage] putString('$key', '$value')")
        storedData[key] = value
    }

    override fun getToken(): String? = null

    override fun saveToken(token: String) {}

    override fun getRefreshToken(): String? = null

    override fun saveRefreshToken(token: String) {}

    override fun isLogined(): Boolean = false

    override fun logout() {}

    override fun contains(key: String): Boolean = storedData.containsKey(key)

    override fun remove(key: String) {
        storedData.remove(key)
    }
}

class MyCityRepositoryMultipleCallsTest {
    @Test
    fun `getSelectedCity should not make multiple searchCities calls when city is already selected`() =
        runTest {
            val mockDictionary = MockDictionaryWithCallTracking()
            val mockStorage = MockStorageWithCallTracking()

            mockStorage.putString("my_city_id", "158830")
            mockStorage.putString("my_city_name", "Москва")

            val repository = MyCityRepositoryImpl(mockDictionary, mockStorage)

            println("\n=== Тест 1: getSelectedCity с сохраненным городом ===")
            val city1 = repository.getSelectedCity.first()
            println("Результат: ${city1.name} (id: ${city1.id})")
            println("Количество вызовов searchCities: ${mockDictionary.searchCitiesCallCount}")

            mockDictionary.searchCitiesCallCount = 0

            println("\n=== Тест 2: Повторный вызов getSelectedCity ===")
            val city2 = repository.getSelectedCity.first()
            println("Результат: ${city2.name} (id: ${city2.id})")
            println("Количество вызовов searchCities: ${mockDictionary.searchCitiesCallCount}")

            assertEquals(city1.id, city2.id)
            assertTrue(
                mockDictionary.searchCitiesCallCount <= 1,
                "Ожидалось не более 1 вызова searchCities, но было ${mockDictionary.searchCitiesCallCount}",
            )
        }

    @Test
    fun `getSelectedCity should make minimal searchCities calls when city name is provided`() =
        runTest {
            val mockDictionary = MockDictionaryWithCallTracking()
            val mockStorage = MockStorageWithCallTracking()

            mockStorage.putString("my_city_id", "123")
            mockStorage.putString("my_city_name", "Росалина")

            val repository = MyCityRepositoryImpl(mockDictionary, mockStorage)

            println("\n=== Тест: getSelectedCity с именем города ===")
            val city = repository.getSelectedCity.first()
            println("Результат: ${city.name} (id: ${city.id})")
            println("Количество вызовов searchCities: ${mockDictionary.searchCitiesCallCount}")
            println("Последний запрос: ${mockDictionary.lastSearchQuery}")

            assertEquals("Росалина", city.name)
            assertEquals(
                0,
                mockDictionary.searchCitiesCallCount,
                "Когда есть ID и имя, searchCities не должен вызываться",
            )
        }

    @Test
    fun `getSelectedCity should handle case when city is not found by name`() =
        runTest {
            val mockDictionary = MockDictionaryWithCallTracking()
            mockDictionary.searchResults =
                mapOf(
                    "Москва" to listOf(City(id = 158830, name = "Москва")),
                )
            val mockStorage = MockStorageWithCallTracking()

            mockStorage.putString("my_city_id", "999")
            mockStorage.putString("my_city_name", "")

            val repository = MyCityRepositoryImpl(mockDictionary, mockStorage)

            println("\n=== Тест: getSelectedCity когда город не найден по ID ===")
            val city = repository.getSelectedCity.first()
            println("Результат: ${city.name} (id: ${city.id})")
            println("Количество вызовов searchCities: ${mockDictionary.searchCitiesCallCount}")

            assertEquals("Москва", city.name)
            assertTrue(
                mockDictionary.searchCitiesCallCount <= 2,
                "Ожидалось не более 2 вызовов searchCities (по ID + Москва), но было ${mockDictionary.searchCitiesCallCount}",
            )
        }

    @Test
    fun `multiple calls to getSelectedCity should not multiply searchCities calls`() =
        runTest {
            val mockDictionary = MockDictionaryWithCallTracking()
            val mockStorage = MockStorageWithCallTracking()

            mockStorage.putString("my_city_id", "158830")
            mockStorage.putString("my_city_name", "Москва")

            val repository = MyCityRepositoryImpl(mockDictionary, mockStorage)

            println("\n=== Тест: Множественные вызовы getSelectedCity ===")

            repeat(3) { index ->
                println("\n--- Вызов #${index + 1} ---")
                mockDictionary.searchCitiesCallCount = 0
                val city = repository.getSelectedCity.first()
                println("Результат: ${city.name} (id: ${city.id})")
                println("Количество вызовов searchCities в этом вызове: ${mockDictionary.searchCitiesCallCount}")
            }
        }
}
