package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FiltersCarSearchRepositoryTest {
    @Test
    fun `searchCarsByUserCity calls carService search with filter model and no geo`() =
        runTest {
            val carService = RecordingCarService()
            carService.searchResult = CarSearchResponse(emptyList(), totalCount = 3, totalPages = 1)
            val filters =
                CarSearchFilters(
                    cityId = "158830",
                    dateFrom = "2025-02-01",
                    dateTo = "2025-02-15",
                    dailyPriceMin = 1000,
                    dailyPriceMax = 5000,
                    brandId = 10,
                    modelId = 20,
                    seatsMin = 5,
                    bodyTypes = listOf("suv"),
                    driveTypes = listOf("awd"),
                    yearMin = 2020,
                    yearMax = 2024,
                    availableMileagePerDayKmMin = 200,
                    page = 2,
                )
            val repository =
                FiltersCarSearchRepository(
                    carService = carService,
                    filters = filters,
                )

            val response = repository.searchCarsByUserCity.first()

            assertEquals("158830", carService.searchCityId)
            assertEquals("2025-02-01", carService.searchDateFrom)
            assertEquals("2025-02-15", carService.searchDateTo)
            assertEquals(1000, carService.searchDailyPriceMin)
            assertEquals(5000, carService.searchDailyPriceMax)
            assertEquals(10, carService.searchBrandId)
            assertEquals(20, carService.searchModelId)
            assertEquals(5, carService.searchSeatsMin)
            assertEquals(listOf("suv"), carService.searchBodyTypes)
            assertEquals(listOf("awd"), carService.searchDriveTypes)
            assertEquals(2020, carService.searchYearMin)
            assertEquals(2024, carService.searchYearMax)
            assertEquals(200, carService.searchAvailableMileagePerDayKmMin)
            assertEquals(2, carService.searchPage)
            assertNull(carService.searchGeoLat)
            assertNull(carService.searchGeoLon)
            assertNull(carService.searchRadiusKm)
            assertEquals(3, response.totalCount)
            assertEquals(1, repository.currentPage.first())
        }
}

private class RecordingCarService : Car {
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
        searchCityId = cityId
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
        searchGeoLat = geoLat
        searchGeoLon = geoLon
        searchRadiusKm = radiusKm
        searchPage = page
        searchPageSize = pageSize
        return searchResult
    }

    override suspend fun getMyCars() = throw NotImplementedError()

    override suspend fun getCar(id: String) = throw NotImplementedError()

    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) = throw NotImplementedError()

    override suspend fun createOrUpdateCar(
        request: my.drivebit.network.services.CarCreateRequest,
        carId: String?,
    ) = throw NotImplementedError()

    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
}
