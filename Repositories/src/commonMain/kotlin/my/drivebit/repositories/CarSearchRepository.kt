package my.drivebit.repositories

import my.drivebit.network.services.Car
import my.drivebit.network.services.CarSearchResponse

interface CarSearchRepository {
    suspend fun searchCarsByUserCity(
        dateFrom: String? = null,
        dateTo: String? = null,
    ): CarSearchResponse
}

internal class CarSearchRepositoryImpl(
    private val carService: Car,
    private val myCityRepository: MyCityRepository,
) : CarSearchRepository {
    override suspend fun searchCarsByUserCity(
        dateFrom: String?,
        dateTo: String?,
    ): CarSearchResponse {
        val selectedCity = myCityRepository.getSelectedCity()
        return carService.search(
            cityId = selectedCity.id.toString(),
            dateFrom = dateFrom,
            dateTo = dateTo,
        )
    }
}
