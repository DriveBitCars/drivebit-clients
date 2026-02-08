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
        ) { values ->
            val selectedCity = values[0] as City
            val currentTaskShortName = values[1] as String?
            val startDate = values[2] as String?
            val endDate = values[3] as String?
            val dailyRateMin = values[4] as Double?
            val dailyRateMax = values[5] as Double?
            val brandId = values[6] as Int?
            Triple(
                Quadruple(selectedCity, currentTaskShortName, startDate, endDate),
                Pair(dailyRateMin, dailyRateMax),
                brandId,
            )
        }.flatMapLatest { (quadruple, priceRange, brandId) ->
            val selectedCity = quadruple.first
            val currentTaskShortName = quadruple.second
            val startDate = quadruple.third
            val endDate = quadruple.fourth
            val dailyRateMin = priceRange.first
            val dailyRateMax = priceRange.second
            flow {
                val filter =
                    currentTaskShortName?.let { shortName ->
                        dictionary
                            .getFiltersSuggested()
                            .firstOrNull { it.shortName == shortName }
                    }

                val result =
                    carService.search(
                        cityId = selectedCity.id.toString(),
                        dateFrom = startDate,
                        dateTo = endDate,
                        availableMileagePerDayKmMin = filter?.availableMileagePerDayKmMin,
                        dailyPriceMin = dailyRateMin?.toInt() ?: filter?.dailyPriceMin,
                        dailyPriceMax = dailyRateMax?.toInt() ?: filter?.dailyPriceMax,
                        yearMin = filter?.yearMin,
                        yearMax = filter?.yearMax,
                        seatsMin = filter?.seatsMin,
                        seatsMax = filter?.seatsMax,
                        bodyTypes = filter?.bodyTypes?.map { it.name },
                        engineTypes = filter?.engineTypes?.map { it.name },
                        colors = filter?.colors?.map { it.name },
                        brandId = brandId,
                    )

                emit(result)
            }
        }
}
