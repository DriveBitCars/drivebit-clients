package my.drivebit.repositories

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
        var selectedCity: City = City(id = 158830, name = "Москва")

        override suspend fun searchCities(query: String): List<City> = throw NotImplementedError()

        override suspend fun getSelectedCity(): City = selectedCity

        override fun selectCity(
            cityId: Int,
            cityName: String,
        ) {
            selectedCity = City(id = cityId, name = cityName)
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

            val result = repository.searchCarsByUserCity()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(null, mockCarService.searchDateFrom)
            assertEquals(null, mockCarService.searchDateTo)
            assertEquals(2, result.cars.size)
            assertEquals("BMW", result.cars[0].brand)
            assertEquals("Audi", result.cars[1].brand)
        }

    @Test
    fun `should search cars with dates`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 3, name = "Калининград")
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            repository.searchCarsByUserCity(
                dateFrom = "2024-01-01",
                dateTo = "2024-01-10",
            )

            assertEquals("3", mockCarService.searchCityId)
            assertEquals("2024-01-01", mockCarService.searchDateFrom)
            assertEquals("2024-01-10", mockCarService.searchDateTo)
        }

    @Test
    fun `should use different city when MyCityRepository returns different city`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158831, name = "Санкт-Петербург")
            val repository = CarSearchRepositoryImpl(mockCarService, mockMyCityRepository)

            repository.searchCarsByUserCity()

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
                    repository.searchCarsByUserCity()
                }.exceptionOrNull()

            assertIs<Exception>(exception)
            assertEquals("City not found", exception?.message)
        }
}
