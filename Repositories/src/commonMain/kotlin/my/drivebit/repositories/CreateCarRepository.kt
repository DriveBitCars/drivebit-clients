package my.drivebit.repositories

import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarResponse

interface CreateCarRepository {
    suspend fun createCar(dailyRate: Double): Result<CarResponse>
}

internal class CreateCarRepositoryImpl(
    private val carDataRepository: CarDataRepository,
    private val selectedCarBrandRepository: SelectedCarBrandRepository,
    private val selectedCarModelRepository: SelectedCarModelRepository,
    private val selectedBodyTypeRepository: SelectedBodyTypeRepository,
    private val selectedDriveTypeRepository: SelectedDriveTypeRepository,
    private val selectedEngineTypeRepository: SelectedEngineTypeRepository,
    private val licensePlateRepository: LicensePlateRepository,
    private val selectedAddressRepository: SelectedAddressRepository,
    private val selectedCityRepository: SelectedCityRepository,
    private val myCarRepository: MyCarRepository,
    private val carService: Car,
) : CreateCarRepository {
    override suspend fun createCar(dailyRate: Double): Result<CarResponse> =
        runCatching {
            val request =
                CarCreateRequest(
                    brandId = selectedCarBrandRepository.getBrandId(),
                    modelId = selectedCarModelRepository.getModelId(),
                    bodyType = selectedBodyTypeRepository.getBodyTypeName(),
                    driveType = selectedDriveTypeRepository.getDriveTypeName(),
                    engineType = selectedEngineTypeRepository.getEngineTypeName(),
                    engineVolume = carDataRepository.getEngineVolume(),
                    productionYear = carDataRepository.getProductionYear(),
                    seatsCount = carDataRepository.getSeatsCount(),
                    licensePlate = licensePlateRepository.getLicensePlate(),
                    ValidAddressString = selectedAddressRepository.getAddress() ?: "",
                    cityId = selectedCityRepository.getCityId()?.toString(),
                    addr = null,
                    hourlyRate = 0.0,
                    dailyRate = dailyRate,
                    ParkingAssistances = emptyList(),
                    MultimediaSystemOptions = emptyList(),
                )

            val response = carService.createCar(request)
            val finalResponse = ensureCarId(response)

            myCarRepository.refresh()
            carDataRepository.saveCarId(finalResponse.id)

            finalResponse
        }

    private suspend fun ensureCarId(response: CarResponse): CarResponse {
        if (response.id.isNotBlank()) return response

        myCarRepository.refresh()
        val cars = myCarRepository.getMyCar()
        val firstCarId = cars.firstOrNull()?.id
        return if (!firstCarId.isNullOrBlank()) {
            CarResponse(id = firstCarId)
        } else {
            response
        }
    }
}
