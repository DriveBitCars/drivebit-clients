package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class CarSearchRepositoryTest {
    private class MockCarService : Car {
        var searchCityId: String? = null
        var searchDateFrom: String? = null
        var searchDateTo: String? = null
        var searchAvailableMileagePerDayKmMin: Int? = null
        var searchDailyPriceMin: Int? = null
        var searchDailyPriceMax: Int? = null
        var searchYearMin: Int? = null
        var searchYearMax: Int? = null
        var searchSeatsMin: Int? = null
        var searchSeatsMax: Int? = null
        var searchBodyTypes: List<String>? = null
        var searchEngineTypes: List<String>? = null
        var searchColors: List<String>? = null
        var searchBrandId: Int? = null
        var searchModelId: Int? = null
        var searchDriveTypes: List<String>? = null
        var searchAllowedTravelDestinations: List<String>? = null
        var searchGeoLat: Double? = null
        var searchGeoLon: Double? = null
        var searchRadiusKm: Double? = null
        var searchPage: Int = 1
        var searchPageSize: Int = 9
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
            brandId: Int?,
            modelId: Int?,
            driveTypes: List<String>?,
            allowedTravelDestinations: List<String>?,
            geoLat: Double?,
            geoLon: Double?,
            radiusKm: Double?,
            page: Int,
            pageSize: Int,
        ): CarSearchResponse {
            if (shouldThrowError) {
                throw Exception(errorMessage)
            }
            searchCityId = cityId
            searchGeoLat = geoLat
            searchGeoLon = geoLon
            searchRadiusKm = radiusKm
            searchDateFrom = dateFrom
            searchDateTo = dateTo
            searchAvailableMileagePerDayKmMin = availableMileagePerDayKmMin
            searchDailyPriceMin = dailyPriceMin
            searchDailyPriceMax = dailyPriceMax
            searchYearMin = yearMin
            searchYearMax = yearMax
            searchSeatsMin = seatsMin
            searchSeatsMax = seatsMax
            searchBodyTypes = bodyTypes
            searchEngineTypes = engineTypes
            searchColors = colors
            searchBrandId = brandId
            searchModelId = modelId
            searchDriveTypes = driveTypes
            searchAllowedTravelDestinations = allowedTravelDestinations
            searchPage = page
            searchPageSize = pageSize
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

    private fun testCarItem(
        id: String,
        brandName: String,
        modelName: String,
    ): CarItem =
        CarItem(
            id = id,
            general =
                CarGeneral(
                    brandName = brandName,
                    modelName = modelName,
                    vin = "VIN123",
                    seats = 4,
                    address =
                        CarAddress(
                            geoLat = 0.0,
                            geoLon = 0.0,
                        ),
                ),
        )

    private fun repository(
        carService: MockCarService,
        request: HomeSearchRequest,
    ) = CarSearchMainRepositoryImpl(
        carService = carService,
        request = request,
    )

    @Test
    fun `should search cars using cityId from request`() =
        runTest {
            val mockCarService = MockCarService()
            val expectedCars =
                listOf(
                    testCarItem(id = "1", brandName = "BMW", modelName = "X5"),
                    testCarItem(id = "2", brandName = "Audi", modelName = "A4"),
                )
            mockCarService.searchResult = CarSearchResponse(expectedCars)
            val result =
                repository(
                    mockCarService,
                    HomeSearchRequest(cityId = "158830"),
                ).searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertNull(mockCarService.searchDateFrom)
            assertNull(mockCarService.searchDateTo)
            assertEquals(2, result.cars.size)
            assertEquals("BMW", result.cars[0].general.brandName)
            assertEquals("Audi", result.cars[1].general.brandName)
            assertEquals(9, mockCarService.searchPageSize)
            assertEquals(1, mockCarService.searchPage)
        }

    @Test
    fun `should pass geo params when nearby filter and center is set`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(
                    cityId = "158830",
                    taskShortName = "Поблизости",
                    lat = 55.75,
                    lon = 37.62,
                    radiusKm = 25,
                    page = 3,
                ),
            ).searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(55.75, mockCarService.searchGeoLat)
            assertEquals(37.62, mockCarService.searchGeoLon)
            assertEquals(25.0, mockCarService.searchRadiusKm)
            assertEquals(100, mockCarService.searchPageSize)
            assertEquals(1, mockCarService.searchPage)
        }

    @Test
    fun `should not pass geo when nearby without center`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(
                    cityId = "158830",
                    taskShortName = "Поблизости",
                ),
            ).searchCarsByUserCity.first()

            assertNull(mockCarService.searchGeoLat)
            assertNull(mockCarService.searchGeoLon)
            assertNull(mockCarService.searchRadiusKm)
            assertEquals(100, mockCarService.searchPageSize)
        }

    @Test
    fun `should use page from request for normal search`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repo =
                repository(
                    mockCarService,
                    HomeSearchRequest(cityId = "158830", page = 2),
                )
            repo.searchCarsByUserCity.first()

            assertEquals(2, mockCarService.searchPage)
            assertEquals(9, mockCarService.searchPageSize)
            assertEquals(1, repo.currentPage.first())
        }

    @Test
    fun `should use different cityId from request`() =
        runTest {
            val mockCarService = MockCarService()
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158831"),
            ).searchCarsByUserCity.first()

            assertEquals("158831", mockCarService.searchCityId)
        }

    @Test
    fun `should propagate error from Car service`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.shouldThrowError = true
            mockCarService.errorMessage = "City not found"
            val exception =
                runCatching {
                    repository(
                        mockCarService,
                        HomeSearchRequest(cityId = "158830"),
                    ).searchCarsByUserCity.first()
                }.exceptionOrNull()

            assertIs<Exception>(exception)
            assertEquals("City not found", exception?.message)
        }

    @Test
    fun `should pass Minivan body type when task is Miniven`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Минивэн"),
            ).searchCarsByUserCity.first()

            assertEquals(listOf("Minivan"), mockCarService.searchBodyTypes)
        }

    @Test
    fun `should pass SUV body type when task is Vnedorozhnik`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Внедорожник"),
            ).searchCarsByUserCity.first()

            assertEquals(listOf("SUV"), mockCarService.searchBodyTypes)
        }

    @Test
    fun `should pass econom dailyPriceMax when task is Ekonom`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Эконом"),
            ).searchCarsByUserCity.first()

            assertNull(mockCarService.searchDailyPriceMin)
            assertEquals(2500, mockCarService.searchDailyPriceMax)
        }

    @Test
    fun `should pass premium dailyPriceMin when task is Premium`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Премиум"),
            ).searchCarsByUserCity.first()

            assertEquals(7000, mockCarService.searchDailyPriceMin)
            assertNull(mockCarService.searchDailyPriceMax)
        }

    @Test
    fun `should pass komfort dailyPrice range when task is Komfort`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Комфорт"),
            ).searchCarsByUserCity.first()

            assertEquals(2500, mockCarService.searchDailyPriceMin)
            assertEquals(4000, mockCarService.searchDailyPriceMax)
        }

    @Test
    fun `should pass biznes dailyPrice range when task is Biznes`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Бизнес"),
            ).searchCarsByUserCity.first()

            assertEquals(4000, mockCarService.searchDailyPriceMin)
            assertEquals(7000, mockCarService.searchDailyPriceMax)
        }

    @Test
    fun `should pass Crimea AllowedTravelDestinations when task is V Krym`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "В Крым"),
            ).searchCarsByUserCity.first()

            assertEquals(listOf("Crimea"), mockCarService.searchAllowedTravelDestinations)
        }

    @Test
    fun `should pass Abkhazia AllowedTravelDestinations when task is Abkhazia`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830", taskShortName = "Абхазия"),
            ).searchCarsByUserCity.first()

            assertEquals(listOf("Abkhazia"), mockCarService.searchAllowedTravelDestinations)
        }

    @Test
    fun `should pass start date and end date from request`() =
        runTest {
            val mockCarService = MockCarService()
            repository(
                mockCarService,
                HomeSearchRequest(
                    cityId = "158830",
                    dateFrom = "2025-02-01",
                    dateTo = "2025-02-15",
                ),
            ).searchCarsByUserCity.first()

            assertEquals("2025-02-01", mockCarService.searchDateFrom)
            assertEquals("2025-02-15", mockCarService.searchDateTo)
        }

    @Test
    fun `should pass null dates when dates are not set`() =
        runTest {
            val mockCarService = MockCarService()
            repository(
                mockCarService,
                HomeSearchRequest(cityId = "158830"),
            ).searchCarsByUserCity.first()

            assertNull(mockCarService.searchDateFrom)
            assertNull(mockCarService.searchDateTo)
        }

    @Test
    fun `refreshSearch triggers another network call`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repo = repository(mockCarService, HomeSearchRequest(cityId = "158830"))
            repo.searchCarsByUserCity.first()
            repo.refreshSearch()
            repo.searchCarsByUserCity.first()
            assertEquals("158830", mockCarService.searchCityId)
        }
}
