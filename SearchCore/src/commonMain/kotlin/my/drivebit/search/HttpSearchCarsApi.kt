package my.drivebit.search

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import my.drivebit.network.services.CarDTOPagedResult

private const val BASE_URL = "https://drivebit.ru/api/"

class HttpSearchCarsApi(
    private val httpClient: HttpClient,
) : SearchCarsApi {
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
        val url = "${BASE_URL}Car/list/filtered/$cityId"
        val paged: CarDTOPagedResult =
            httpClient
                .get(url) {
                    parameter("page", page)
                    parameter("pageSize", pageSize)
                    dateFrom?.let { parameter("BookingStart", it) }
                    dateTo?.let { parameter("BookingEnd", it) }
                    availableMileagePerDayKmMin?.let { parameter("AvailableMileagePerDayKmMin", it) }
                    dailyPriceMin?.let { parameter("DailyRateMin", it) }
                    dailyPriceMax?.let { parameter("DailyRateMax", it) }
                    yearMin?.let { parameter("YearMin", it) }
                    yearMax?.let { parameter("YearMax", it) }
                    seatsMin?.let { parameter("SeatsMin", it) }
                    bodyTypes?.forEach { parameter("BodyType", it) }
                    brandId?.let { parameter("BrandId", it) }
                    modelId?.let { parameter("ModelId", it) }
                    driveTypes?.forEach { parameter("DriveType", it) }
                }.body()
        return SearchCarsResult(
            cars = paged.items,
            totalCount = paged.totalCount,
            totalPages = paged.totalPages,
        )
    }
}
