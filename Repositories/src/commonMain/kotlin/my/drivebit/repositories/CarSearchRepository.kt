package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.City

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)

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
    private val selectedCity: Flow<City>,
    private val currentFiltersRepository: CurrentFiltersRepository,
) : CarSearchRepository {
    private val searchRefreshNonce = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        combine(
            selectedCity,
            searchRefreshNonce,
            currentFiltersRepository.currentTaskShortName,
            currentFiltersRepository.startState,
            currentFiltersRepository.endState,
            currentFiltersRepository.dailyRateMin,
            currentFiltersRepository.dailyRateMax,
            currentFiltersRepository.brandId,
            currentFiltersRepository.modelId,
            currentFiltersRepository.driveTypeName,
            currentFiltersRepository.bodyTypeName,
            currentFiltersRepository.seatsMin,
        ) { values ->
            val city = values[0] as City
            val currentTaskShortName = values[2] as String?
            val startDate = values[3] as String?
            val endDate = values[4] as String?
            val dailyRateMin = values[5] as Int?
            val dailyRateMax = values[6] as Int?
            val brandId = values[7] as Int?
            val modelId = values[8] as Int?
            val driveTypeName = values[9] as String?
            val bodyTypeName = values[10] as String?
            val seatsMin = values[11] as Int?
            Triple(
                Quadruple(city, currentTaskShortName, startDate, endDate),
                Pair(dailyRateMin, dailyRateMax),
                Quintuple(brandId, modelId, driveTypeName, bodyTypeName, seatsMin),
            )
        }.combine(
            combine(
                currentFiltersRepository.engineTypeName,
                currentFiltersRepository.colorName,
                currentFiltersRepository.yearMin,
                currentFiltersRepository.yearMax,
                currentFiltersRepository.seatsMax,
                currentFiltersRepository.availableMileagePerDayKmMin,
            ) { values ->
                Quadruple(
                    Pair(values[0] as String?, values[1] as String?),
                    Pair(values[2] as Int?, values[3] as Int?),
                    values[4] as Int?,
                    values[5] as Int?,
                )
            },
        ) { mainFilters, extraFilters ->
            Pair(mainFilters, extraFilters)
        }.combine(currentFiltersRepository.currentPage) { filtersPair, page ->
            Triple(filtersPair.first, filtersPair.second, page)
        }.combine(
            combine(
                currentFiltersRepository.nearbySearchLat,
                currentFiltersRepository.nearbySearchLon,
                currentFiltersRepository.nearbyRadiusKm,
            ) { lat, lon, radiusKm ->
                Triple(lat, lon, radiusKm)
            },
        ) { filtersTriple, nearbyParams ->
            Pair(filtersTriple, nearbyParams)
        }.flatMapLatest { (filtersTriple, nearbyParams) ->
            val (mainFilters, extraFilters, page) = filtersTriple
            val (nearbyLat, nearbyLon, nearbyRadiusKm) = nearbyParams
            val (quadruple, priceRange, brandModelDriveBodySeats) = mainFilters
            val brandId = brandModelDriveBodySeats.first
            val modelId = brandModelDriveBodySeats.second
            val driveTypeName = brandModelDriveBodySeats.third
            val bodyTypeName = brandModelDriveBodySeats.fourth
            val seatsMin = brandModelDriveBodySeats.fifth
            val selectedCity = quadruple.first
            val currentTaskShortName = quadruple.second
            val startDate = quadruple.third
            val endDate = quadruple.fourth
            val dailyRateMin = priceRange.first
            val dailyRateMax = priceRange.second
            val engineTypeName = extraFilters.first.first
            val colorName = extraFilters.first.second
            val yearMin = extraFilters.second.first
            val yearMax = extraFilters.second.second
            val seatsMax = extraFilters.third
            val availableMileagePerDayKmMin = extraFilters.fourth
            flow {
                val filter =
                    if (currentTaskShortName == "Все") {
                        null
                    } else {
                        currentTaskShortName?.let { shortName ->
                            SuggestedFiltersCatalog.findByShortName(shortName)
                        }
                    }

                val (effectivePage, effectivePageSize) =
                    if (currentTaskShortName == "Поблизости") {
                        Pair(0, MAP_NEARBY_PAGE_SIZE)
                    } else {
                        Pair(page, PAGE_SIZE)
                    }

                val useNearbyGeo =
                    currentTaskShortName == "Поблизости" &&
                        nearbyLat != null &&
                        nearbyLon != null

                val result =
                    carService.search(
                        cityId = selectedCity.id.toString(),
                        dateFrom = startDate,
                        dateTo = endDate,
                        availableMileagePerDayKmMin = availableMileagePerDayKmMin ?: filter?.availableMileagePerDayKmMin,
                        dailyPriceMin = dailyRateMin ?: filter?.dailyPriceMin,
                        dailyPriceMax = dailyRateMax ?: filter?.dailyPriceMax,
                        yearMin = yearMin ?: filter?.yearMin,
                        yearMax = yearMax ?: filter?.yearMax,
                        seatsMin = seatsMin ?: filter?.seatsMin,
                        seatsMax = seatsMax ?: filter?.seatsMax,
                        bodyTypes = bodyTypeName?.let { listOf(it) } ?: filter?.bodyTypes?.map { it.name },
                        engineTypes = engineTypeName?.let { listOf(it) } ?: filter?.engineTypes?.map { it.name },
                        colors = colorName?.let { listOf(it) } ?: filter?.colors?.map { it.name },
                        brandId = brandId,
                        modelId = modelId,
                        driveTypes = driveTypeName?.let { listOf(it) },
                        allowedTravelDestinations = filter?.allowedTravelDestinations,
                        geoLat = if (useNearbyGeo) nearbyLat else null,
                        geoLon = if (useNearbyGeo) nearbyLon else null,
                        radiusKm = if (useNearbyGeo) nearbyRadiusKm.toDouble() else null,
                        page = effectivePage + 1,
                        pageSize = effectivePageSize,
                    )

                emit(result)
            }
        }

    override val currentPage: Flow<Int> = currentFiltersRepository.currentPage

    override fun setPage(page: Int) {
        currentFiltersRepository.setPage(page)
    }

    override fun refreshSearch() {
        searchRefreshNonce.value++
    }
}
