package my.drivebit.search

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

private const val BASE_URL = "https://drivebit.ru/api/"

@Serializable
private data class CarDtoPagedResult(
    val items: List<CarDtoItem> = emptyList(),
    val totalCount: Int = 0,
    val totalPages: Int = 0,
)

@Serializable
private data class CarDtoItem(
    val id: String,
    val general: CarDtoGeneral? = null,
    val price: Double? = null,
)

@Serializable
private data class CarDtoGeneral(
    val brandName: String? = null,
    val modelName: String? = null,
)

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
        val paged: CarDtoPagedResult =
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
            cars =
                paged.items.map { item ->
                    val brand = item.general?.brandName.orEmpty()
                    val model = item.general?.modelName.orEmpty()
                    SearchCarCard(
                        id = item.id,
                        title = listOf(brand, model).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { item.id },
                        price = item.price?.takeIf { it > 0 }?.toInt(),
                    )
                },
            totalCount = paged.totalCount,
            totalPages = paged.totalPages,
        )
    }
}

fun resolveSearchCityId(citySlug: String?): String =
    when (citySlug?.lowercase()) {
        "kaliningrad" -> "158831"
        "rostov-na-donu" -> "158832"
        else -> "158830"
    }
