package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.Dictionary

interface CarSearchRepository {
    val searchCarsByUserCity: Flow<CarSearchResponse>
}

internal class CarSearchRepositoryImpl(
    private val carService: Car,
    private val myCityRepository: MyCityRepository,
    private val currentTaskRepository: CurrentTaskRepository,
    private val dictionary: Dictionary,
) : CarSearchRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        combine(
            myCityRepository.getSelectedCity,
            currentTaskRepository.currentTaskShortName,
        ) { selectedCity, currentTaskShortName ->
            selectedCity to currentTaskShortName
        }.flatMapLatest { (selectedCity, currentTaskShortName) ->
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
                        dateFrom = null,
                        dateTo = null,
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
                    )

                emit(result)
            }
        }
}
