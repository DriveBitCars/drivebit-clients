package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarResponse
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.CreateCarRepository
import my.drivebit.repositories.HasPassportRepo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class FakeCarDataRepository : CarDataRepository {
    private var dailyRate: Int? = null

    override fun saveEngineVolume(volume: Double) {}

    override fun getEngineVolume(): Double? = 2.0

    override fun saveProductionYear(year: Int) {}

    override fun getProductionYear(): Int? = 2022

    override fun saveSeatsCount(count: Int) {}

    override fun getSeatsCount(): Int? = 5

    override fun saveCarId(carId: String) {}

    override fun getCarId(): String? = null

    override fun saveHourlyRate(rate: Int) {}

    override fun getHourlyRate(): Int? = null

    override fun saveDailyRate(rate: Int) {
        dailyRate = rate
    }

    override fun getDailyRate(): Int? = dailyRate

    override fun saveDailyRate4Days(rate: Int?) {}

    override fun getDailyRate4Days(): Int? = null

    override fun saveDailyRate7Days(rate: Int?) {}

    override fun getDailyRate7Days(): Int? = null

    override fun saveDailyRate14Days(rate: Int?) {}

    override fun getDailyRate14Days(): Int? = null

    override fun saveDailyRate21Days(rate: Int?) {}

    override fun getDailyRate21Days(): Int? = null

    override fun saveSeasonalPriceAdjustmentPercent(percent: Int?) {}

    override fun getSeasonalPriceAdjustmentPercent(): Int? = null

    override fun savePrepaymentPercent(percent: Int?) {}

    override fun getPrepaymentPercent(): Int? = null

    override fun saveMonthlyRate(rate: Int) {}

    override fun getMonthlyRate(): Int? = null

    override fun saveDescription(description: String) {}

    override fun getDescription(): String? = null

    override fun clearAll() {}
}

private class FakeHasPassportRepo(
    private val hasPassport: Boolean,
) : HasPassportRepo {
    override suspend fun hasPasport(): Boolean = hasPassport
}

private class FakeCreateCarRepository(
    private val result: Result<CarResponse>,
) : CreateCarRepository {
    override suspend fun createCar(dailyRate: Int): Result<CarResponse> = result
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateCarFromDailyRateViewModelTest {
    @Test
    fun `submitDailyRate creates car when passport is present`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carDataRepo = FakeCarDataRepository()
            val viewModel =
                CreateCarFromDailyRateViewModelImpl(
                    carDataRepository = carDataRepo,
                    createCarRepository = FakeCreateCarRepository(Result.success(CarResponse(id = "car-1"))),
                    hasPassportRepo = FakeHasPassportRepo(true),
                    coroutineScope = testScope,
                )

            viewModel.submitDailyRate(1000)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CreateCarFromDailyRateState.Success>(state)
            assertEquals("car-1", state.carId)
        }

    @Test
    fun `submitDailyRate requires passport when missing`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carDataRepo = FakeCarDataRepository()
            val viewModel =
                CreateCarFromDailyRateViewModelImpl(
                    carDataRepository = carDataRepo,
                    createCarRepository = FakeCreateCarRepository(Result.success(CarResponse(id = "car-1"))),
                    hasPassportRepo = FakeHasPassportRepo(false),
                    coroutineScope = testScope,
                )

            viewModel.submitDailyRate(1000)
            advanceUntilIdle()

            assertIs<CreateCarFromDailyRateState.MissingPassport>(viewModel.state.value)
        }
}
