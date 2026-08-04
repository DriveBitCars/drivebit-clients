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

    fun saveHourlyRate(rate: Int)

    fun getHourlyRate(): Int?

    fun saveDailyRate(rate: Int)

    fun getDailyRate(): Int?

    fun saveDailyRate4Days(rate: Int?)

    fun getDailyRate4Days(): Int?

    fun saveDailyRate7Days(rate: Int?)

    fun getDailyRate7Days(): Int?

    fun saveDailyRate14Days(rate: Int?)

    fun getDailyRate14Days(): Int?

    fun saveDailyRate21Days(rate: Int?)

    fun getDailyRate21Days(): Int?

    fun saveSeasonalPriceAdjustmentPercent(percent: Int?)

    fun getSeasonalPriceAdjustmentPercent(): Int?

    fun savePrepaymentPercent(percent: Int?)

    fun getPrepaymentPercent(): Int?

    fun saveMonthlyRate(rate: Int)

    fun getMonthlyRate(): Int?

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
        private const val DAILY_RATE_4_DAYS_KEY = "car_daily_rate_4_days"
        private const val DAILY_RATE_7_DAYS_KEY = "car_daily_rate_7_days"
        private const val DAILY_RATE_14_DAYS_KEY = "car_daily_rate_14_days"
        private const val DAILY_RATE_21_DAYS_KEY = "car_daily_rate_21_days"
        private const val SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY = "car_seasonal_price_adjustment_percent"
        private const val PREPAYMENT_PERCENT_KEY = "car_prepayment_percent"
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

    override fun saveHourlyRate(rate: Int) {
        settings.putInt(HOURLY_RATE_KEY, rate)
    }

    override fun getHourlyRate(): Int? {
        val rate = settings.getInt(HOURLY_RATE_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate(rate: Int) {
        settings.putInt(DAILY_RATE_KEY, rate)
    }

    override fun getDailyRate(): Int? {
        val rate = settings.getInt(DAILY_RATE_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate4Days(rate: Int?) {
        if (rate != null && rate >= 0) {
            settings.putInt(DAILY_RATE_4_DAYS_KEY, rate)
        } else {
            settings.remove(DAILY_RATE_4_DAYS_KEY)
        }
    }

    override fun getDailyRate4Days(): Int? {
        val rate = settings.getInt(DAILY_RATE_4_DAYS_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate7Days(rate: Int?) {
        if (rate != null && rate >= 0) {
            settings.putInt(DAILY_RATE_7_DAYS_KEY, rate)
        } else {
            settings.remove(DAILY_RATE_7_DAYS_KEY)
        }
    }

    override fun getDailyRate7Days(): Int? {
        val rate = settings.getInt(DAILY_RATE_7_DAYS_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate14Days(rate: Int?) {
        if (rate != null && rate >= 0) {
            settings.putInt(DAILY_RATE_14_DAYS_KEY, rate)
        } else {
            settings.remove(DAILY_RATE_14_DAYS_KEY)
        }
    }

    override fun getDailyRate14Days(): Int? {
        val rate = settings.getInt(DAILY_RATE_14_DAYS_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveDailyRate21Days(rate: Int?) {
        if (rate != null && rate >= 0) {
            settings.putInt(DAILY_RATE_21_DAYS_KEY, rate)
        } else {
            settings.remove(DAILY_RATE_21_DAYS_KEY)
        }
    }

    override fun getDailyRate21Days(): Int? {
        val rate = settings.getInt(DAILY_RATE_21_DAYS_KEY, -1)
        return if (rate < 0) null else rate
    }

    override fun saveSeasonalPriceAdjustmentPercent(percent: Int?) {
        if (percent != null && percent in -90..1000) {
            settings.putInt(SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY, percent)
        } else {
            settings.remove(SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY)
        }
    }

    override fun getSeasonalPriceAdjustmentPercent(): Int? {
        if (!settings.hasKey(SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY)) return null
        return settings.getInt(SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY, 0)
    }

    override fun savePrepaymentPercent(percent: Int?) {
        if (percent != null && percent in 0..100) {
            settings.putInt(PREPAYMENT_PERCENT_KEY, percent)
        } else {
            settings.remove(PREPAYMENT_PERCENT_KEY)
        }
    }

    override fun getPrepaymentPercent(): Int? {
        val percent = settings.getInt(PREPAYMENT_PERCENT_KEY, -1)
        return if (percent < 0) null else percent
    }

    override fun saveMonthlyRate(rate: Int) {
        settings.putInt(MONTHLY_RATE_KEY, rate)
    }

    override fun getMonthlyRate(): Int? {
        val rate = settings.getInt(MONTHLY_RATE_KEY, -1)
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
        settings.remove(DAILY_RATE_4_DAYS_KEY)
        settings.remove(DAILY_RATE_7_DAYS_KEY)
        settings.remove(DAILY_RATE_14_DAYS_KEY)
        settings.remove(DAILY_RATE_21_DAYS_KEY)
        settings.remove(SEASONAL_PRICE_ADJUSTMENT_PERCENT_KEY)
        settings.remove(PREPAYMENT_PERCENT_KEY)
        settings.remove(MONTHLY_RATE_KEY)
        settings.remove(DESCRIPTION_KEY)
    }
}
