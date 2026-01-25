package my.drivebit.repositories

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.City
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CarSearchRepositoryTest {
    private class MockCarService : Car {
        var searchCityId: String? = null
        var searchDateFrom: String? = null
        var searchDateTo: String? = null
        var searchResult: CarSearchResponse = CarSearchResponse(emptyList())
        var shouldThrowError = false
        var errorMessage = "Network error"

        override suspend fun search(
            cityId: String,
            dateFrom: String?,
            dateTo: String?,
            availableMileagePerDayKmMin: Int?,
            dailyPriceMin: Int?,
            dailyPriceMax: Int?,
            yearMin: Int?,
            yearMax: Int?,
            seatsMin: Int?,
            seatsMax: Int?,
            bodyTypes: List<String>?,
            engineTypes: List<String>?,
            colors: List<String>?,
        ): CarSearchResponse {
            if (shouldThrowError) {
                throw Exception(errorMessage)
            }
            searchCityId = cityId
            searchDateFrom = dateFrom
            searchDateTo = dateTo
            return searchResult
        }

        override suspend fun getMyCars(): List<CarItem> = throw NotImplementedError()

        override suspend fun getCar(id: String) = throw NotImplementedError()

        override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
            throw NotImplementedError()

        override suspend fun createOrUpdateCar(
            request: my.drivebit.network.services.CarCreateRequest,
            carId: String?,
        ) = throw NotImplementedError()

        override suspend fun deleteCar(carId: String) = throw NotImplementedError()
    }

    private class MockMyCityRepository : MyCityRepository {
        private val selectedCityFlow = MutableStateFlow<City>(City(id = 158830, name = "Москва"))
        var selectedCity: City
            get() = selectedCityFlow.value
            set(value) {
                selectedCityFlow.value = value
            }

        override suspend fun searchCities(query: String): List<City> = throw NotImplementedError()

        override val getSelectedCity: Flow<City> = selectedCityFlow

        override fun selectCity(
            cityId: Int,
            cityName: String,
        ) {
            selectedCityFlow.value = City(id = cityId, name = cityName)
        }
    }

    @Test
    fun `should search cars using selected city from MyCityRepository`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val expectedCars =
                listOf(
                    CarItem(id = "1", brand = "BMW", model = "X5"),
                    CarItem(id = "2", brand = "Audi", model = "A4"),
                )
            mockCarService.searchResult = CarSearchResponse(expectedCars)
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            val result = repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(null, mockCarService.searchDateFrom)
            assertEquals(null, mockCarService.searchDateTo)
            assertEquals(2, result.cars.size)
            assertEquals("BMW", result.cars[0].brand)
            assertEquals("Audi", result.cars[1].brand)
        }

    @Test
    fun `should use different city when MyCityRepository returns different city`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158831, name = "Санкт-Петербург")
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            repository.searchCarsByUserCity.first()

            assertEquals("158831", mockCarService.searchCityId)
        }

    @Test
    fun `should propagate error from Car service`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.shouldThrowError = true
            mockCarService.errorMessage = "City not found"
            val mockMyCityRepository = MockMyCityRepository()
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            val exception =
                runCatching {
                    repository.searchCarsByUserCity.first()
                }.exceptionOrNull()

            assertIs<Exception>(exception)
            assertEquals("City not found", exception?.message)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when city changes`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val moscowCars = listOf(CarItem(id = "1", brand = "BMW", model = "X5"))
            val spbCars = listOf(CarItem(id = "2", brand = "Audi", model = "A4"))
            mockCarService.searchResult = CarSearchResponse(moscowCars)
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals("158830", mockCarService.searchCityId)
                assertEquals(1, results[0].cars.size)
                assertEquals("BMW", results[0].cars[0].brand)

                mockCarService.searchResult = CarSearchResponse(spbCars)
                mockMyCityRepository.selectCity(158831, "Санкт-Петербург")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals("158831", mockCarService.searchCityId)
            assertEquals(1, results[1].cars.size)
            assertEquals("Audi", results[1].cars[0].brand)
        }
}
