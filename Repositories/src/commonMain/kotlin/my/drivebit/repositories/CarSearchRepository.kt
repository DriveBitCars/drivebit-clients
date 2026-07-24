package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.utils.HOME_DEFAULT_NEARBY_RADIUS_KM

private const val PAGE_SIZE = 9
private const val MAP_NEARBY_PAGE_SIZE = 100

interface CarSearchRepository {
    val searchCarsByUserCity: Flow<CarSearchResponse>
    val currentPage: Flow<Int>

    fun setPage(page: Int)

    fun refreshSearch()
}

data class CarSearchFilters(
    val cityId: String,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val availableMileagePerDayKmMin: Int? = null,
    val dailyPriceMin: Int? = null,
    val dailyPriceMax: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val seatsMin: Int? = null,
    val seatsMax: Int? = null,
    val bodyTypes: List<String>? = null,
    val engineTypes: List<String>? = null,
    val colors: List<String>? = null,
    val brandId: Int? = null,
    val modelId: Int? = null,
    val driveTypes: List<String>? = null,
    val allowedTravelDestinations: List<String>? = null,
    val page: Int = 1,
)

data class HomeSearchRequest(
    val cityId: String,
    val taskShortName: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val page: Int = 1,
    val lat: Double? = null,
    val lon: Double? = null,
    val radiusKm: Int = HOME_DEFAULT_NEARBY_RADIUS_KM,
)

internal class FiltersCarSearchRepository(
    private val carService: Car,
    private val filters: CarSearchFilters,
    private val pageSize: Int = PAGE_SIZE,
) : CarSearchRepository {
    private val refreshNonce = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        refreshNonce.flatMapLatest {
            flow {
                emit(
                    carService.search(
                        cityId = filters.cityId,
                        dateFrom = filters.dateFrom,
                        dateTo = filters.dateTo,
                        availableMileagePerDayKmMin = filters.availableMileagePerDayKmMin,
                        dailyPriceMin = filters.dailyPriceMin,
                        dailyPriceMax = filters.dailyPriceMax,
                        yearMin = filters.yearMin,
                        yearMax = filters.yearMax,
                        seatsMin = filters.seatsMin,
                        seatsMax = filters.seatsMax,
                        bodyTypes = filters.bodyTypes,
                        engineTypes = filters.engineTypes,
                        colors = filters.colors,
                        brandId = filters.brandId,
                        modelId = filters.modelId,
                        driveTypes = filters.driveTypes,
                        allowedTravelDestinations = filters.allowedTravelDestinations,
                        geoLat = null,
                        geoLon = null,
                        radiusKm = null,
                        page = filters.page.coerceAtLeast(1),
                        pageSize = pageSize,
                    ),
                )
            }
        }

    override val currentPage: Flow<Int> = flowOf((filters.page.coerceAtLeast(1) - 1))

    override fun setPage(page: Int) = Unit

    override fun refreshSearch() {
        refreshNonce.value++
    }
}

internal class CarSearchMainRepositoryImpl(
    private val carService: Car,
    private val request: HomeSearchRequest,
) : CarSearchRepository {
    private val searchRefreshNonce = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        searchRefreshNonce.flatMapLatest {
            flow {
                val task = request.taskShortName
                val filter =
                    if (task == null || task == "Все") {
                        null
                    } else {
                        SuggestedFiltersCatalog.findByShortName(task)
                    }

                val isNearby = task == "Поблизости"
                val (effectivePage, effectivePageSize) =
                    if (isNearby) {
                        Pair(0, MAP_NEARBY_PAGE_SIZE)
                    } else {
                        Pair((request.page.coerceAtLeast(1) - 1), PAGE_SIZE)
                    }

                val useNearbyGeo =
                    isNearby &&
                        request.lat != null &&
                        request.lon != null

                emit(
                    carService.search(
                        cityId = request.cityId,
                        dateFrom = request.dateFrom,
                        dateTo = request.dateTo,
                        availableMileagePerDayKmMin = filter?.availableMileagePerDayKmMin,
                        dailyPriceMin = filter?.dailyPriceMin,
                        dailyPriceMax = filter?.dailyPriceMax,
                        yearMin = filter?.yearMin,
                        yearMax = filter?.yearMax,
                        seatsMin = filter?.seatsMin,
                        seatsMax = filter?.seatsMax,
                        bodyTypes = filter?.bodyTypes?.map { it.name },
                        engineTypes = filter?.engineTypes?.map { it.name },
                        colors = filter?.colors?.map { it.name },
                        brandId = null,
                        modelId = null,
                        driveTypes = null,
                        allowedTravelDestinations = filter?.allowedTravelDestinations,
                        geoLat = if (useNearbyGeo) request.lat else null,
                        geoLon = if (useNearbyGeo) request.lon else null,
                        radiusKm = if (useNearbyGeo) request.radiusKm.toDouble() else null,
                        page = effectivePage + 1,
                        pageSize = effectivePageSize,
                    ),
                )
            }
        }

    override val currentPage: Flow<Int> = flowOf((request.page.coerceAtLeast(1) - 1))

    override fun setPage(page: Int) = Unit

    override fun refreshSearch() {
        searchRefreshNonce.value++
    }
}
