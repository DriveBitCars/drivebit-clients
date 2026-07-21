package my.drivebit.repositories

import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class MyCarRepositoryTest {
    private fun generalStub() =
        CarGeneral(
            brandName = "Brand",
            modelName = "Model",
            vin = "VIN123",
            seats = 4,
            address =
                CarAddress(
                    geoLat = 0.0,
                    geoLon = 0.0,
                ),
        )

    @Test
    fun `should return cars from network on first call`() =
        runTest {
            val expectedCars =
                listOf(
                    CarItem(
                        id = "car1",
                        general = generalStub(),
                    ),
                    CarItem(
                        id = "car2",
                        general = generalStub(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun search(
                        cityId: String,
                        dateFrom: String?,
                        dateTo: String?,
                        availableMileagePerDayKmMin: Int?,
                        dailyPriceMin: Int?,
                        dailyPriceMax: Int?,
                        yearMin: Int?,
                        yearMax: Int?,
                        seatsMin: Int?,
                        seatsMax: Int?,
                        bodyTypes: List<String>?,
                        engineTypes: List<String>?,
                        colors: List<String>?,
                        brandId: Int?,
                        modelId: Int?,
                        driveTypes: List<String>?,
                        allowedTravelDestinations: List<String>?,
                        geoLat: Double?,
                        geoLon: Double?,
                        radiusKm: Double?,
                        page: Int,
                        pageSize: Int,
                    ) = throw NotImplementedError()

                    override suspend fun getMyCars(): List<CarItem> {
                        callCount++
                        return expectedCars
                    }

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
                }

            val repository = MyCarRepositoryImpl(fakeCarService, InMemorySettings())

            val result = repository.getMyCar()

            assertEquals(1, callCount)
            assertEquals(expectedCars, result)
        }

    @Test
    fun `should return cached cars on subsequent calls`() =
        runTest {
            val firstCars =
                listOf(
                    CarItem(
                        id = "car1",
                        general = generalStub(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun search(
                        cityId: String,
                        dateFrom: String?,
                        dateTo: String?,
                        availableMileagePerDayKmMin: Int?,
                        dailyPriceMin: Int?,
                        dailyPriceMax: Int?,
                        yearMin: Int?,
                        yearMax: Int?,
                        seatsMin: Int?,
                        seatsMax: Int?,
                        bodyTypes: List<String>?,
                        engineTypes: List<String>?,
                        colors: List<String>?,
                        brandId: Int?,
                        modelId: Int?,
                        driveTypes: List<String>?,
                        allowedTravelDestinations: List<String>?,
                        geoLat: Double?,
                        geoLon: Double?,
                        radiusKm: Double?,
                        page: Int,
                        pageSize: Int,
                    ) = throw NotImplementedError()

                    override suspend fun getMyCars(): List<CarItem> {
                        callCount++
                        return firstCars
                    }

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
                }

            val repository = MyCarRepositoryImpl(fakeCarService, InMemorySettings())

            val firstResult = repository.getMyCar()
            val secondResult = repository.getMyCar()
            val thirdResult = repository.getMyCar()

            assertEquals(1, callCount)
            assertEquals(firstCars, firstResult)
            assertEquals(firstCars, secondResult)
            assertEquals(firstCars, thirdResult)
        }

    @Test
    fun `should fetch from network after refresh`() =
        runTest {
            val firstCars =
                listOf(
                    CarItem(
                        id = "car1",
                        general = generalStub(),
                    ),
                )

            val secondCars =
                listOf(
                    CarItem(
                        id = "car2",
                        general = generalStub(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun search(
                        cityId: String,
                        dateFrom: String?,
                        dateTo: String?,
                        availableMileagePerDayKmMin: Int?,
                        dailyPriceMin: Int?,
                        dailyPriceMax: Int?,
                        yearMin: Int?,
                        yearMax: Int?,
                        seatsMin: Int?,
                        seatsMax: Int?,
                        bodyTypes: List<String>?,
                        engineTypes: List<String>?,
                        colors: List<String>?,
                        brandId: Int?,
                        modelId: Int?,
                        driveTypes: List<String>?,
                        allowedTravelDestinations: List<String>?,
                        geoLat: Double?,
                        geoLon: Double?,
                        radiusKm: Double?,
                        page: Int,
                        pageSize: Int,
                    ) = throw NotImplementedError()

                    override suspend fun getMyCars(): List<CarItem> {
                        callCount++
                        return if (callCount == 1) firstCars else secondCars
                    }

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
                }

            val repository = MyCarRepositoryImpl(fakeCarService, InMemorySettings())

            val firstResult = repository.getMyCar()
            repository.refresh()
            val secondResult = repository.getMyCar()

            assertEquals(2, callCount)
            assertEquals(firstCars, firstResult)
            assertEquals(secondCars, secondResult)
        }

    @Test
    fun `should cache after refresh`() =
        runTest {
            val cars =
                listOf(
                    CarItem(
                        id = "car1",
                        general = generalStub(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun search(
                        cityId: String,
                        dateFrom: String?,
                        dateTo: String?,
                        availableMileagePerDayKmMin: Int?,
                        dailyPriceMin: Int?,
                        dailyPriceMax: Int?,
                        yearMin: Int?,
                        yearMax: Int?,
                        seatsMin: Int?,
                        seatsMax: Int?,
                        bodyTypes: List<String>?,
                        engineTypes: List<String>?,
                        colors: List<String>?,
                        brandId: Int?,
                        modelId: Int?,
                        driveTypes: List<String>?,
                        allowedTravelDestinations: List<String>?,
                        geoLat: Double?,
                        geoLon: Double?,
                        radiusKm: Double?,
                        page: Int,
                        pageSize: Int,
                    ) = throw NotImplementedError()

                    override suspend fun getMyCars(): List<CarItem> {
                        callCount++
                        return cars
                    }

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun deleteCar(carId: String) = throw NotImplementedError()
                }

            val repository = MyCarRepositoryImpl(fakeCarService, InMemorySettings())

            repository.getMyCar()
            repository.refresh()
            repository.getMyCar()
            repository.getMyCar()

            assertEquals(2, callCount)
        }
}
