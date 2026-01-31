package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface CarDataRepository {
    fun saveEngineVolume(volume: Double)

    fun getEngineVolume(): Double?

    fun saveProductionYear(year: Int)

    fun getProductionYear(): Int?

    fun saveSeatsCount(count: Int)

    fun getSeatsCount(): Int?

    fun saveCarId(carId: String)

    fun getCarId(): String?

    fun saveHourlyRate(rate: Double)

    fun getHourlyRate(): Double?

    fun saveDailyRate(rate: Double)

    fun getDailyRate(): Double?

    fun saveMonthlyRate(rate: Double)

    fun getMonthlyRate(): Double?

    fun saveDescription(description: String)

    fun getDescription(): String?

    fun clearAll()
}

internal class CarDataRepositoryImpl(
    private val settings: Settings,
) : CarDataRepository {
    companion object {
        private const val ENGINE_VOLUME_KEY = "car_engine_volume"
        private const val PRODUCTION_YEAR_KEY = "car_production_year"
        private const val SEATS_COUNT_KEY = "car_seats_count"
        private const val CAR_ID_KEY = "car_id"
        private const val HOURLY_RATE_KEY = "car_hourly_rate"
        private const val DAILY_RATE_KEY = "car_daily_rate"
        private const val MONTHLY_RATE_KEY = "car_monthly_rate"
        private const val DESCRIPTION_KEY = "car_description"
    }

    override fun saveEngineVolume(volume: Double) {
        settings.putDouble(ENGINE_VOLUME_KEY, volume)
    }

    override fun getEngineVolume(): Double? {
        val volume = settings.getDouble(ENGINE_VOLUME_KEY, -1.0)
        return if (volume < 0) null else volume
    }

    override fun saveProductionYear(year: Int) {
        settings.putInt(PRODUCTION_YEAR_KEY, year)
    }

    override fun getProductionYear(): Int? {
        val year = settings.getInt(PRODUCTION_YEAR_KEY, -1)
        return if (year == -1) null else year
    }

    override fun saveSeatsCount(count: Int) {
        settings.putInt(SEATS_COUNT_KEY, count)
    }

    override fun getSeatsCount(): Int? {
        val count = settings.getInt(SEATS_COUNT_KEY, -1)
        return if (count == -1) null else count
    }

    override fun saveCarId(carId: String) {
        settings.putString(CAR_ID_KEY, carId)
    }

    override fun getCarId(): String? = settings.getStringOrNullIfEmpty(CAR_ID_KEY)

    override fun saveHourlyRate(rate: Double) {
        settings.putDouble(HOURLY_RATE_KEY, rate)
    }

    override fun getHourlyRate(): Double? {
        val rate = settings.getDouble(HOURLY_RATE_KEY, -1.0)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate(rate: Double) {
        settings.putDouble(DAILY_RATE_KEY, rate)
    }

    override fun getDailyRate(): Double? {
        val rate = settings.getDouble(DAILY_RATE_KEY, -1.0)
        return if (rate < 0) null else rate
    }

    override fun saveMonthlyRate(rate: Double) {
        settings.putDouble(MONTHLY_RATE_KEY, rate)
    }

    override fun getMonthlyRate(): Double? {
        val rate = settings.getDouble(MONTHLY_RATE_KEY, -1.0)
        return if (rate < 0) null else rate
    }

    override fun saveDescription(description: String) {
        settings.putString(DESCRIPTION_KEY, description)
    }

    override fun getDescription(): String? = settings.getStringOrNullIfEmpty(DESCRIPTION_KEY)

    override fun clearAll() {
        settings.remove(ENGINE_VOLUME_KEY)
        settings.remove(PRODUCTION_YEAR_KEY)
        settings.remove(SEATS_COUNT_KEY)
        settings.remove(CAR_ID_KEY)
        settings.remove(HOURLY_RATE_KEY)
        settings.remove(DAILY_RATE_KEY)
        settings.remove(MONTHLY_RATE_KEY)
        settings.remove(DESCRIPTION_KEY)
    }
}
