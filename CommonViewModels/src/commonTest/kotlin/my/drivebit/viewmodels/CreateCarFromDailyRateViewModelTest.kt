package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarResponse
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.HasPassportRepo
import my.drivebit.repositories.LicensePlateRepository
import my.drivebit.repositories.MyCarRepository
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedBodyTypeRepository
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.repositories.SelectedCarModelRepository
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.repositories.SelectedDriveTypeRepository
import my.drivebit.repositories.SelectedEngineTypeRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class FakeCarDataRepository : CarDataRepository {
    private var dailyRate: Double? = null

    override fun saveEngineVolume(volume: Double) {}

    override fun getEngineVolume(): Double? = 2.0

    override fun saveProductionYear(year: Int) {}

    override fun getProductionYear(): Int? = 2022

    override fun saveSeatsCount(count: Int) {}

    override fun getSeatsCount(): Int? = 5

    override fun saveCarId(carId: String) {}

    override fun getCarId(): String? = null

    override fun saveHourlyRate(rate: Double) {}

    override fun getHourlyRate(): Double? = null

    override fun saveDailyRate(rate: Double) {
        dailyRate = rate
    }

    override fun getDailyRate(): Double? = dailyRate

    override fun saveMonthlyRate(rate: Double) {}

    override fun getMonthlyRate(): Double? = null

    override fun clearAll() {}
}

private class FakeSelectedCarBrandRepository : SelectedCarBrandRepository {
    override fun saveBrand(
        brandId: Int,
        brandName: String,
    ) {}

    override fun getBrandId(): Int? = 1

    override fun getBrandName(): String? = "Brand"

    override fun clearBrand() {}
}

private class FakeSelectedCarModelRepository : SelectedCarModelRepository {
    override fun saveModel(
        modelId: Int,
        modelName: String,
    ) {}

    override fun getModelId(): Int? = 2

    override fun getModelName(): String? = "Model"

    override fun clearModel() {}
}

private class FakeSelectedBodyTypeRepository : SelectedBodyTypeRepository {
    override fun saveBodyType(
        name: String,
        translate: String,
    ) {}

    override fun getBodyTypeName(): String? = "SUV"

    override fun getBodyTypeTranslate(): String? = "Внедорожник"

    override fun clearBodyType() {}
}

private class FakeSelectedDriveTypeRepository : SelectedDriveTypeRepository {
    override fun saveDriveType(
        name: String,
        translate: String,
    ) {}

    override fun getDriveTypeName(): String? = "Rear"

    override fun getDriveTypeTranslate(): String? = "Задний"

    override fun clearDriveType() {}
}

private class FakeSelectedEngineTypeRepository : SelectedEngineTypeRepository {
    override fun saveEngineType(
        name: String,
        translate: String,
    ) {}

    override fun getEngineTypeName(): String? = "Diesel"

    override fun getEngineTypeTranslate(): String? = "Дизель"

    override fun clearEngineType() {}
}

private class FakeLicensePlateRepository : LicensePlateRepository {
    override fun saveLicensePlate(licensePlate: String) {}

    override fun getLicensePlate(): String? = "A123BC"

    override fun clearLicensePlate() {}
}

private class FakeSelectedAddressRepository : SelectedAddressRepository {
    override fun saveAddress(address: String) {}

    override fun getAddress(): String? = "Address"

    override fun saveAddressData(addressData: my.drivebit.network.services.AddressData?) {}

    override fun getAddressData(): my.drivebit.network.services.AddressData? = null

    override fun clearAddress() {}
}

private class FakeSelectedCityRepositoryForCreateCar : SelectedCityRepository {
    override fun saveCity(
        cityId: Int,
        cityName: String,
    ) {}

    override fun getCityId(): Int? = 10

    override fun getCityName(): String? = "City"

    override fun clearCity() {}
}

private class FakeMyCarRepository : MyCarRepository {
    override suspend fun getMyCar(): List<CarItem> = emptyList()

    override fun refresh() {}
}

private class FakeHasPassportRepo(
    private val hasPassport: Boolean,
) : HasPassportRepo {
    override suspend fun hasPasport(): Boolean = hasPassport
}

private class FakeCarService(
    private val response: CarResponse,
) : Car {
    override suspend fun search(
        cityId: String,
        dateFrom: String?,
        dateTo: String?,
    ) = throw NotImplementedError()

    override suspend fun getMyCars(): List<CarItem> = throw NotImplementedError()

    override suspend fun getCar(carId: String): CarDetailResponse = throw NotImplementedError()

    override suspend fun createCar(request: CarCreateRequest): CarResponse = response

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse = throw NotImplementedError()

    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateCarFromDailyRateViewModelTest {
    @Test
    fun `submitDailyRate creates car when passport is present`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                CreateCarFromDailyRateViewModelImpl(
                    carDataRepository = FakeCarDataRepository(),
                    selectedCarBrandRepository = FakeSelectedCarBrandRepository(),
                    selectedCarModelRepository = FakeSelectedCarModelRepository(),
                    selectedBodyTypeRepository = FakeSelectedBodyTypeRepository(),
                    selectedDriveTypeRepository = FakeSelectedDriveTypeRepository(),
                    selectedEngineTypeRepository = FakeSelectedEngineTypeRepository(),
                    licensePlateRepository = FakeLicensePlateRepository(),
                    selectedAddressRepository = FakeSelectedAddressRepository(),
                    selectedCityRepository = FakeSelectedCityRepositoryForCreateCar(),
                    myCarRepository = FakeMyCarRepository(),
                    carService = FakeCarService(CarResponse(id = "car-1")),
                    hasPassportRepo = FakeHasPassportRepo(true),
                    coroutineScope = testScope,
                )

            viewModel.submitDailyRate(1000.0)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CreateCarFromDailyRateState.Success>(state)
            assertEquals("car-1", state.carId)
        }

    @Test
    fun `submitDailyRate requires passport when not skipping`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                CreateCarFromDailyRateViewModelImpl(
                    carDataRepository = FakeCarDataRepository(),
                    selectedCarBrandRepository = FakeSelectedCarBrandRepository(),
                    selectedCarModelRepository = FakeSelectedCarModelRepository(),
                    selectedBodyTypeRepository = FakeSelectedBodyTypeRepository(),
                    selectedDriveTypeRepository = FakeSelectedDriveTypeRepository(),
                    selectedEngineTypeRepository = FakeSelectedEngineTypeRepository(),
                    licensePlateRepository = FakeLicensePlateRepository(),
                    selectedAddressRepository = FakeSelectedAddressRepository(),
                    selectedCityRepository = FakeSelectedCityRepositoryForCreateCar(),
                    myCarRepository = FakeMyCarRepository(),
                    carService = FakeCarService(CarResponse(id = "car-1")),
                    hasPassportRepo = FakeHasPassportRepo(false),
                    coroutineScope = testScope,
                )

            viewModel.submitDailyRate(1000.0)
            advanceUntilIdle()

            assertIs<CreateCarFromDailyRateState.MissingPassport>(viewModel.state.value)
        }
}
