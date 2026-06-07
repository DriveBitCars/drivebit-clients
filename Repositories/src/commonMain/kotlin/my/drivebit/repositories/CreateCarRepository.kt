package my.drivebit.repositories

import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarResponse
import my.drivebit.network.services.DEFAULT_CAR_PREPAYMENT_PERCENT

interface CreateCarRepository {
    suspend fun createCar(dailyRate: Int): Result<CarResponse>
}

internal class CreateCarRepositoryImpl(
    private val carDataRepository: CarDataRepository,
    private val selectedCarBrandRepository: SelectedCarBrandRepository,
    private val selectedCarModelRepository: SelectedCarModelRepository,
    private val selectedBodyTypeRepository: SelectedBodyTypeRepository,
    private val selectedDriveTypeRepository: SelectedDriveTypeRepository,
    private val selectedEngineTypeRepository: SelectedEngineTypeRepository,
    private val selectedTrunkSizeRepository: SelectedTrunkSizeRepository,
    private val licensePlateRepository: LicensePlateRepository,
    private val selectedAddressRepository: SelectedAddressRepository,
    private val selectedCityRepository: SelectedCityRepository,
    private val myCarRepository: MyCarRepository,
    private val carService: Car,
) : CreateCarRepository {
    override suspend fun createCar(dailyRate: Int): Result<CarResponse> =
        runCatching {
            val address = selectedAddressRepository.getAddress() ?: ""
            val trunkSizeName =
                selectedTrunkSizeRepository.getTrunkSizeName()
                    ?: throw IllegalStateException("Trunk size is required")

            println("🚗 [CreateCarRepository] Создание машины:")
            println("   - address: $address")
            println("   - dailyRate: $dailyRate")

            val request =
                CarCreateRequest(
                    brandId = selectedCarBrandRepository.getBrandId(),
                    modelId = selectedCarModelRepository.getModelId(),
                    bodyType = selectedBodyTypeRepository.getBodyTypeName(),
                    driveType = selectedDriveTypeRepository.getDriveTypeName(),
                    engineType = selectedEngineTypeRepository.getEngineTypeName(),
                    engineVolume = carDataRepository.getEngineVolume(),
                    year =
                        carDataRepository.getProductionYear()
                            ?: throw IllegalStateException("Production year is required"),
                    seats = carDataRepository.getSeatsCount(),
                    trunkSize = trunkSizeName,
                    licensePlate = licensePlateRepository.getLicensePlate(),
                    ValidAddressString = address,
                    addr = null,
                    description =
                        carDataRepository
                            .getDescription()
                            ?.takeIf { it.isNotBlank() },
                    hourlyRate = 0,
                    dailyRate = dailyRate,
                    dailyRate4Days = carDataRepository.getDailyRate4Days(),
                    dailyRate7Days = carDataRepository.getDailyRate7Days(),
                    dailyRate14Days = carDataRepository.getDailyRate14Days(),
                    dailyRate21Days = carDataRepository.getDailyRate21Days(),
                    prepaymentPercent = DEFAULT_CAR_PREPAYMENT_PERCENT,
                    ParkingAssistances = emptyList(),
                    MultimediaSystemOptions = emptyList(),
                )

            // Выбор города при создании машины отключён: город вводится вместе с адресом, cityId = null

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
