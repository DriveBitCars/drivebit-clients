package my.drivebit.search

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchCarRepositoryTest {
    @Test
    fun `results flow emits api response for constructor filters without geo`() =
        runTest {
            val api = RecordingSearchApi()
            api.response =
                SearchCarsResult(
                    cars = listOf(testCarItem(id = "1", brand = "BMW", model = "X5")),
                    totalCount = 1,
                    totalPages = 1,
                )
            val filters =
                SearchFilterSet(
                    citySlug = "moskva",
                    brandId = 10,
                    modelId = 20,
                    startDate = "2025-02-01",
                    endDate = "2025-02-15",
                    dailyRateMin = 1000,
                    seatsMin = 5,
                    page = 2,
                )
            val repository =
                SearchCarRepositoryImpl(
                    api = api,
                    filters = filters,
                    cityIdResolver = { "158830" },
                )

            val result = repository.results.first()

            assertEquals("158830", api.lastCityId)
            assertEquals(10, api.lastBrandId)
            assertEquals(20, api.lastModelId)
            assertEquals("2025-02-01", api.lastDateFrom)
            assertEquals("2025-02-15", api.lastDateTo)
            assertEquals(1000, api.lastDailyPriceMin)
            assertEquals(5, api.lastSeatsMin)
            assertEquals(2, api.lastPage)
            assertNull(api.lastGeoLat)
            assertNull(api.lastGeoLon)
            assertNull(api.lastRadiusKm)
            assertEquals(1, result.cars.size)
            assertEquals("BMW", result.cars[0].general.brandName)
            assertEquals("X5", result.cars[0].general.modelName)
        }
}

internal fun testCarItem(
    id: String = "1",
    brand: String = "BMW",
    model: String = "X5",
): CarItem =
    CarItem(
        id = id,
        year = 2020,
        price = 5000.0,
        photos = emptyList(),
        general =
            CarGeneral(
                brandName = brand,
                modelName = model,
                seats = 5,
                address = CarAddress(),
            ),
    )

private class RecordingSearchApi : SearchCarsApi {
    var response: SearchCarsResult = SearchCarsResult()
    var lastCityId: String? = null
    var lastBrandId: Int? = null
    var lastModelId: Int? = null
    var lastDateFrom: String? = null
    var lastDateTo: String? = null
    var lastDailyPriceMin: Int? = null
    var lastSeatsMin: Int? = null
    var lastPage: Int? = null
    var lastGeoLat: Double? = null
    var lastGeoLon: Double? = null
    var lastRadiusKm: Double? = null

    override suspend fun searchCars(
        cityId: String,
        dateFrom: String?,
        dateTo: String?,
        availableMileagePerDayKmMin: Int?,
        dailyPriceMin: Int?,
        dailyPriceMax: Int?,
        yearMin: Int?,
        yearMax: Int?,
        seatsMin: Int?,
        bodyTypes: List<String>?,
        brandId: Int?,
        modelId: Int?,
        driveTypes: List<String>?,
        geoLat: Double?,
        geoLon: Double?,
        radiusKm: Double?,
        page: Int,
        pageSize: Int,
    ): SearchCarsResult {
        lastCityId = cityId
        lastBrandId = brandId
        lastModelId = modelId
        lastDateFrom = dateFrom
        lastDateTo = dateTo
        lastDailyPriceMin = dailyPriceMin
        lastSeatsMin = seatsMin
        lastPage = page
        lastGeoLat = geoLat
        lastGeoLon = geoLon
        lastRadiusKm = radiusKm
        return response
    }
}
