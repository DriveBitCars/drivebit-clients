package my.drivebit.utils

import kotlin.math.round

const val SEASONAL_PERCENT_MIN = -90
const val SEASONAL_PERCENT_MAX = 1000

fun clampSeasonalPercent(percent: Double): Double =
    percent.coerceIn(SEASONAL_PERCENT_MIN.toDouble(), SEASONAL_PERCENT_MAX.toDouble())

fun applySeasonalAdjustment(
    rate: Double,
    adjustmentPercent: Double,
): Double {
    val percent = clampSeasonalPercent(adjustmentPercent)
    if (percent == 0.0) return rate
    return roundAwayFromZero(rate * (1.0 + percent / 100.0), decimals = 2)
}

fun removeSeasonalAdjustment(
    adjustedRate: Double,
    adjustmentPercent: Double,
): Double {
    val percent = clampSeasonalPercent(adjustmentPercent)
    if (percent == 0.0) return adjustedRate
    val factor = 1.0 + percent / 100.0
    return roundAwayFromZero(adjustedRate / factor, decimals = 2)
}

fun parseSeasonalPercentInput(raw: String): Int? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val value = trimmed.toIntOrNull() ?: return null
    if (value < SEASONAL_PERCENT_MIN || value > SEASONAL_PERCENT_MAX) return null
    return value
}

fun isoDateTimeToLocalDate(iso: String): String = iso.trim().take(10)

fun localDateToIsoDateTime(date: String): String = "${date.trim()}T00:00:00.000Z"

fun validateSeasonalPriceAdjustmentPeriod(
    startsAt: String,
    endsAt: String,
    percent: String,
): String? {
    if (startsAt.isBlank() || endsAt.isBlank()) {
        return "Укажите даты периода"
    }
    if (endsAt < startsAt) {
        return "Дата окончания не может быть раньше даты начала"
    }
    if (parseSeasonalPercentInput(percent) == null) {
        return "Процент от -90 до 1000"
    }
    return null
}

private fun roundAwayFromZero(
    value: Double,
    decimals: Int,
): Double {
    var factor = 1.0
    repeat(decimals) { factor *= 10.0 }
    val scaled = value * factor
    val rounded =
        if (scaled >= 0.0) {
            round(scaled + 1e-9)
        } else {
            -round(-scaled + 1e-9)
        }
    return rounded / factor
}
