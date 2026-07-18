package my.drivebit.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import my.drivebit.network.services.CarItem

data class SearchFilterSet(
    val citySlug: String? = null,
    val brandSlug: String? = null,
    val modelSlug: String? = null,
    val brandId: Int? = null,
    val brandName: String? = null,
    val modelId: Int? = null,
    val modelName: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val dailyRateMin: Int? = null,
    val dailyRateMax: Int? = null,
    val driveType: String? = null,
    val driveTypeLabel: String? = null,
    val bodyType: String? = null,
    val bodyTypeLabel: String? = null,
    val seatsMin: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val mileageMin: Int? = null,
    val page: Int = 1,
)

inline fun applySearchFilterChange(
    current: SearchFilterSet,
    transform: (SearchFilterSet) -> SearchFilterSet,
): SearchFilterSet = transform(current).copy(page = 1)

data class SearchCarsResult(
    val cars: List<CarItem> = emptyList(),
    val totalCount: Int = 0,
    val totalPages: Int = 0,
)

interface SearchCarsApi {
    suspend fun searchCars(
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
    ): SearchCarsResult
}

interface SearchCarRepository {
    val results: Flow<SearchCarsResult>
}

class SearchCarRepositoryImpl(
    private val api: SearchCarsApi,
    private val filters: SearchFilterSet,
    private val resolveCityId: suspend () -> String,
    private val pageSize: Int = 9,
) : SearchCarRepository {
    override val results: Flow<SearchCarsResult> =
        flow {
            val cityId = resolveCityId()
            emit(
                api.searchCars(
                    cityId = cityId,
                    dateFrom = filters.startDate,
                    dateTo = filters.endDate,
                    availableMileagePerDayKmMin = filters.mileageMin,
                    dailyPriceMin = filters.dailyRateMin,
                    dailyPriceMax = filters.dailyRateMax,
                    yearMin = filters.yearMin,
                    yearMax = filters.yearMax,
                    seatsMin = filters.seatsMin,
                    bodyTypes = filters.bodyType?.let { listOf(it) },
                    brandId = filters.brandId,
                    modelId = filters.modelId,
                    driveTypes = filters.driveType?.let { listOf(it) },
                    geoLat = null,
                    geoLon = null,
                    radiusKm = null,
                    page = filters.page.coerceAtLeast(1),
                    pageSize = pageSize,
                ),
            )
        }
}
