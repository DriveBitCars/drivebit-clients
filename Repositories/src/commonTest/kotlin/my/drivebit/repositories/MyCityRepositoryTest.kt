package my.drivebit.repositories

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.CarEnumsResponse
import my.drivebit.network.services.CarModel
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.DocumentEnumsResponse
import my.drivebit.network.services.FilterSuggestion
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MyCityRepositoryTest {
    private fun createTestStorage(): Storage = TestStorage()

    private class TestStorage : Storage {
        private val storage = mutableMapOf<String, String>()

        override fun isLogined(): Boolean = storage["auth_token"]?.isNotEmpty() == true

        override fun saveToken(token: String) {
            storage["auth_token"] = token
        }

        override fun getToken(): String? = storage["auth_token"]?.takeIf { it.isNotEmpty() }

        override fun saveRefreshToken(refreshToken: String) {
            storage["refresh_token"] = refreshToken
        }

        override fun getRefreshToken(): String? = storage["refresh_token"]?.takeIf { it.isNotEmpty() }

        override fun logout() {
            storage.clear()
        }

        override fun putString(
            key: String,
            value: String,
        ) {
            storage[key] = value
        }

        override fun getString(
            key: String,
            defaultValue: String,
        ): String = storage[key] ?: defaultValue

        override fun contains(key: String): Boolean = storage.containsKey(key)

        override fun remove(key: String) {
            storage.remove(key)
        }
    }

    private fun createMockDictionary(searchResults: Map<String, List<City>> = emptyMap()): Dictionary =
        object : Dictionary {
            override suspend fun getCarBrands(): List<CarBrand> = throw NotImplementedError()

            override suspend fun getCarModels(brandId: Int): List<CarModel> = throw NotImplementedError()

            override suspend fun getCarBrandsExisting(): List<CarBrand> = throw NotImplementedError()

            override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> = throw NotImplementedError()

            override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

            override suspend fun searchCities(query: String): List<City> = searchResults[query] ?: emptyList()

            override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()

            override suspend fun getAllCities(): List<City> = emptyList()

            override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
        }

    @Test
    fun `should search cities by query`() =
        runTest {
            val expectedCities =
                listOf(
                    City(id = 1, name = "Москва"),
                    City(id = 2, name = "Санкт-Петербург"),
                )
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(City(id = 1, name = "Москва")),
                            "Санкт" to expectedCities,
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val result = repository.searchCities("Санкт")

            assertEquals(expectedCities, result)
        }

    @Test
    fun `should select city and save to storage`() =
        runTest {
            val dictionary =
                createMockDictionary(
                    searchResults = emptyMap(),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            repository.selectCity(2, "Санкт-Петербург")

            val selectedCityId = storage.getString(MyCityStorageKeys.ID_KEY)
            val selectedCityName = storage.getString(MyCityStorageKeys.NAME_KEY)
            assertEquals("2", selectedCityId)
            assertEquals("Санкт-Петербург", selectedCityName)
        }

    @Test
    fun `should return Moscow as default city if no city selected`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val selectedCity = repository.getSelectedCity.first()

            assertNotNull(selectedCity)
            assertEquals(158830, selectedCity.id)
            assertEquals("Москва", selectedCity.name)
            val savedCityId = storage.getString(MyCityStorageKeys.ID_KEY)
            val savedCityName = storage.getString(MyCityStorageKeys.NAME_KEY)
            assertEquals("158830", savedCityId)
            assertEquals("Москва", savedCityName)
        }

    @Test
    fun `should return selected city from storage using saved name`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val kaliningradCity = City(id = 3, name = "Калининград")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                            "Калининград" to listOf(kaliningradCity),
                            "" to listOf(kaliningradCity),
                        ),
                )
            val storage = createTestStorage()
            storage.putString(MyCityStorageKeys.ID_KEY, "3")
            storage.putString(MyCityStorageKeys.NAME_KEY, "Калининград")
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val selectedCity = repository.getSelectedCity.first()

            assertNotNull(selectedCity)
            assertEquals(3, selectedCity.id)
            assertEquals("Калининград", selectedCity.name)
        }

    @Test
    fun `should select Moscow as default on first initialization`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val selectedCityBefore = storage.getString(MyCityStorageKeys.ID_KEY)
            assertEquals("", selectedCityBefore)

            val selectedCity = repository.getSelectedCity.first()

            val selectedCityIdAfter = storage.getString(MyCityStorageKeys.ID_KEY)
            val selectedCityNameAfter = storage.getString(MyCityStorageKeys.NAME_KEY)
            assertEquals("158830", selectedCityIdAfter)
            assertEquals("Москва", selectedCityNameAfter)
            assertEquals(158830, selectedCity.id)
            assertEquals("Москва", selectedCity.name)
        }

    @Test
    fun `should update selected city when selectCity called`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val spbCity = City(id = 158831, name = "Санкт-Петербург")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                            "Санкт-Петербург" to listOf(spbCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            repository.selectCity(158830, "Москва")
            assertEquals(158830, repository.getSelectedCity.first().id)

            repository.selectCity(158831, "Санкт-Петербург")
            val selectedCity = repository.getSelectedCity.first()

            assertEquals(158831, selectedCity.id)
            assertEquals("Санкт-Петербург", selectedCity.name)
            val cityIdInStorage = storage.getString(MyCityStorageKeys.ID_KEY)
            val cityNameInStorage = storage.getString(MyCityStorageKeys.NAME_KEY)
            assertEquals("158831", cityIdInStorage)
            assertEquals("Санкт-Петербург", cityNameInStorage)
            assertEquals("sankt-peterburg", storage.getString(MyCityStorageKeys.SLUG_KEY))
        }

    @Test
    fun `should handle city not found in list gracefully`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                            "" to emptyList(),
                        ),
                )
            val storage = createTestStorage()
            storage.putString(MyCityStorageKeys.ID_KEY, "999")
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val selectedCity = repository.getSelectedCity.first()

            assertEquals(158830, selectedCity.id)
            assertEquals("Москва", selectedCity.name)
        }

    @Test
    fun `should search cities from dictionary on each searchCities call`() =
        runTest {
            var callCount = 0
            val dictionary =
                object : Dictionary {
                    override suspend fun getCarBrands(): List<CarBrand> = throw NotImplementedError()

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = throw NotImplementedError()

                    override suspend fun getCarBrandsExisting(): List<CarBrand> = throw NotImplementedError()

                    override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> =
                        throw NotImplementedError()

                    override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

                    override suspend fun searchCities(query: String): List<City> {
                        callCount++
                        return listOf(City(id = 1, name = "Москва"))
                    }

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()

                    override suspend fun getAllCities(): List<City> = throw NotImplementedError()

                    override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
                }
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            repository.searchCities("Москва")
            repository.searchCities("Санкт")
            repository.searchCities("Казань")

            assertEquals(3, callCount)
        }

    @Test
    fun `should not search with empty query when city not found`() =
        runTest {
            var emptyQueryCallCount = 0
            val moscowCity = City(id = 158830, name = "Москва")
            val dictionary =
                object : Dictionary {
                    override suspend fun getCarBrands(): List<CarBrand> = throw NotImplementedError()

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = throw NotImplementedError()

                    override suspend fun getCarBrandsExisting(): List<CarBrand> = throw NotImplementedError()

                    override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> =
                        throw NotImplementedError()

                    override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

                    override suspend fun searchCities(query: String): List<City> {
                        if (query.isEmpty()) {
                            emptyQueryCallCount++
                            return emptyList()
                        }
                        if (query == "Москва") {
                            return listOf(moscowCity)
                        }
                        return emptyList()
                    }

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()

                    override suspend fun getAllCities(): List<City> = throw NotImplementedError()

                    override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
                }
            val storage = createTestStorage()
            storage.putString(MyCityStorageKeys.ID_KEY, "999")
            storage.putString(MyCityStorageKeys.NAME_KEY, "")
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val selectedCity = repository.getSelectedCity.first()

            assertEquals(0, emptyQueryCallCount, "Should not call searchCities with empty query")
            assertEquals(158830, selectedCity.id)
            assertEquals("Москва", selectedCity.name)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `getSelectedCity Flow should emit initial city`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val flow = repository.getSelectedCity
            val firstCity = flow.first()

            assertEquals(158830, firstCity.id)
            assertEquals("Москва", firstCity.name)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `getSelectedCity Flow should emit new city when selectCity is called`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val spbCity = City(id = 158831, name = "Санкт-Петербург")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                            "Санкт-Петербург" to listOf(spbCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val flow = repository.getSelectedCity
            val cities = mutableListOf<City>()

            coroutineScope {
                val job =
                    launch {
                        flow.take(2).toList(cities)
                    }

                advanceUntilIdle()
                assertEquals(1, cities.size)
                assertEquals(158830, cities[0].id)

                repository.selectCity(158831, "Санкт-Петербург")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, cities.size)
            assertEquals(158830, cities[0].id)
            assertEquals(158831, cities[1].id)
            assertEquals("Санкт-Петербург", cities[1].name)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `getSelectedCity Flow should emit multiple cities when selectCity is called multiple times`() =
        runTest {
            val moscowCity = City(id = 158830, name = "Москва")
            val spbCity = City(id = 158831, name = "Санкт-Петербург")
            val kaliningradCity = City(id = 3, name = "Калининград")
            val dictionary =
                createMockDictionary(
                    searchResults =
                        mapOf(
                            "Москва" to listOf(moscowCity),
                            "Санкт-Петербург" to listOf(spbCity),
                            "Калининград" to listOf(kaliningradCity),
                        ),
                )
            val storage = createTestStorage()
            val repository = MyCityRepositoryImpl(dictionary, storage)

            val flow = repository.getSelectedCity
            val cities = mutableListOf<City>()

            coroutineScope {
                val job =
                    launch {
                        flow.take(3).toList(cities)
                    }

                advanceUntilIdle()
                assertEquals(158830, cities[0].id)

                repository.selectCity(158831, "Санкт-Петербург")
                advanceUntilIdle()

                repository.selectCity(3, "Калининград")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(3, cities.size)
            assertEquals(158830, cities[0].id)
            assertEquals(158831, cities[1].id)
            assertEquals(3, cities[2].id)
            assertEquals("Калининград", cities[2].name)
        }
}
