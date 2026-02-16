package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

interface CarSearchRepository {
    val searchCarsByUserCity: Flow<CarSearchResponse>
}

internal class CarSearchRepositoryImpl(
    private val carService: Car,
    private val myCityRepository: MyCityRepository,
    private val currentFiltersRepository: CurrentFiltersRepository,
    private val dictionary: Dictionary,
) : CarSearchRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        combine(
            myCityRepository.getSelectedCity,
            currentFiltersRepository.currentTaskShortName,
            currentFiltersRepository.startState,
            currentFiltersRepository.endState,
            currentFiltersRepository.dailyRateMin,
            currentFiltersRepository.dailyRateMax,
            currentFiltersRepository.brandId,
            currentFiltersRepository.driveTypeName,
            currentFiltersRepository.bodyTypeName,
            currentFiltersRepository.seatsMin,
        ) { values ->
            val selectedCity = values[0] as City
            val currentTaskShortName = values[1] as String?
            val startDate = values[2] as String?
            val endDate = values[3] as String?
            val dailyRateMin = values[4] as Double?
            val dailyRateMax = values[5] as Double?
            val brandId = values[6] as Int?
            val driveTypeName = values[7] as String?
            val bodyTypeName = values[8] as String?
            val seatsMin = values[9] as Int?
            Triple(
                Quadruple(selectedCity, currentTaskShortName, startDate, endDate),
                Pair(dailyRateMin, dailyRateMax),
                Quadruple(brandId, driveTypeName, bodyTypeName, seatsMin),
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
        }.flatMapLatest { (mainFilters, extraFilters) ->
            val (quadruple, priceRange, brandDriveBodySeatsQuadruple) = mainFilters
            val brandId = brandDriveBodySeatsQuadruple.first
            val driveTypeName = brandDriveBodySeatsQuadruple.second
            val bodyTypeName = brandDriveBodySeatsQuadruple.third
            val seatsMin = brandDriveBodySeatsQuadruple.fourth
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
                            dictionary
                                .getFiltersSuggested()
                                .firstOrNull { it.shortName == shortName }
                        }
                    }

                val result =
                    carService.search(
                        cityId = selectedCity.id.toString(),
                        dateFrom = startDate,
                        dateTo = endDate,
                        availableMileagePerDayKmMin = availableMileagePerDayKmMin ?: filter?.availableMileagePerDayKmMin,
                        dailyPriceMin = dailyRateMin?.toInt() ?: filter?.dailyPriceMin,
                        dailyPriceMax = dailyRateMax?.toInt() ?: filter?.dailyPriceMax,
                        yearMin = yearMin ?: filter?.yearMin,
                        yearMax = yearMax ?: filter?.yearMax,
                        seatsMin = seatsMin ?: filter?.seatsMin,
                        seatsMax = seatsMax ?: filter?.seatsMax,
                        bodyTypes = bodyTypeName?.let { listOf(it) } ?: filter?.bodyTypes?.map { it.name },
                        engineTypes = engineTypeName?.let { listOf(it) } ?: filter?.engineTypes?.map { it.name },
                        colors = colorName?.let { listOf(it) } ?: filter?.colors?.map { it.name },
                        brandId = brandId,
                        driveTypes = driveTypeName?.let { listOf(it) },
                    )

                emit(result)
            }
        }
}
