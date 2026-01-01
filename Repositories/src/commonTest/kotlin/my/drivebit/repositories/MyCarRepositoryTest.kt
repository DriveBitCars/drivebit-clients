package my.drivebit.repositories

import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarListItemResponse
import my.drivebit.network.services.GeneralProps
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class MyCarRepositoryTest {
    @Test
    fun `should return cars from network on first call`() =
        runTest {
            val expectedCars =
                listOf(
                    CarListItemResponse(
                        id = "car1",
                        general = GeneralProps(),
                    ),
                    CarListItemResponse(
                        id = "car2",
                        general = GeneralProps(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun getMyCars(): List<CarListItemResponse> {
                        callCount++
                        return expectedCars
                    }

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

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
                    CarListItemResponse(
                        id = "car1",
                        general = GeneralProps(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun getMyCars(): List<CarListItemResponse> {
                        callCount++
                        return firstCars
                    }

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

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
                    CarListItemResponse(
                        id = "car1",
                        general = GeneralProps(),
                    ),
                )

            val secondCars =
                listOf(
                    CarListItemResponse(
                        id = "car2",
                        general = GeneralProps(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun getMyCars(): List<CarListItemResponse> {
                        callCount++
                        return if (callCount == 1) firstCars else secondCars
                    }

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

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
                    CarListItemResponse(
                        id = "car1",
                        general = GeneralProps(),
                    ),
                )

            var callCount = 0
            val fakeCarService =
                object : Car {
                    override suspend fun getMyCars(): List<CarListItemResponse> {
                        callCount++
                        return cars
                    }

                    override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
                        throw NotImplementedError()

                    override suspend fun createOrUpdateCar(
                        request: my.drivebit.network.services.CarCreateRequest,
                        carId: String?,
                    ) = throw NotImplementedError()

                    override suspend fun getCar(carId: String) = throw NotImplementedError()

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
