package my.drivebit.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse

interface CarSearchRepository {
    val searchCarsByUserCity: Flow<CarSearchResponse>
}

internal class CarSearchRepositoryImpl(
    private val carService: Car,
    private val myCityRepository: MyCityRepository,
) : CarSearchRepository {
    override val searchCarsByUserCity: Flow<CarSearchResponse> =
        myCityRepository.getSelectedCity.map { selectedCity ->
            carService.search(
                cityId = selectedCity.id.toString(),
                dateFrom = null,
                dateTo = null,
            )
        }
}
