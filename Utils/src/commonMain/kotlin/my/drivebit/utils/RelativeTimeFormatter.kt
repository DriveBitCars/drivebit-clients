package my.drivebit.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun formatRelativeTime(
    pastInstant: Instant,
    now: Instant = Clock.System.now(),
): String {
    val duration = now - pastInstant
    if (duration.isNegative()) return "только что"

    return when {
        duration < 1.minutes -> "только что"
        duration < 1.hours -> minutesAgo(duration.inWholeMinutes.toInt())
        duration < 24.hours -> hoursAgo(duration.inWholeHours.toInt())
        duration < 7.days -> daysAgo(duration.inWholeDays.toInt())
        duration < 28.days -> weeksAgo((duration.inWholeDays / 7).toInt())
        duration < 365.days -> monthsAgo(monthsBetween(pastInstant, now))
        else -> yearsAgo((duration.inWholeDays / 365).toInt())
    }
}

private fun monthsBetween(
    from: Instant,
    to: Instant,
): Int {
    val fromEpochDays = from.epochSeconds / 86400
    val toEpochDays = to.epochSeconds / 86400
    return ((toEpochDays - fromEpochDays) / 30).toInt().coerceAtLeast(1)
}

private fun minutesAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "минуту"
            n in 2..4 -> "минуты"
            else -> "минут"
        }
    return "$n $word назад"
}

private fun hoursAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "час"
            n in 2..4 -> "часа"
            else -> "часов"
        }
    return "$n $word назад"
}

private fun daysAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "день"
            n in 2..4 -> "дня"
            else -> "дней"
        }
    return "$n $word назад"
}

private fun weeksAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "неделю"
            n in 2..4 -> "недели"
            else -> "недель"
        }
    return "$n $word назад"
}

private fun monthsAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "месяц"
            n in 2..4 -> "месяца"
            else -> "месяцев"
        }
    return "$n $word назад"
}

private fun yearsAgo(n: Int): String {
    val word =
        when {
            n == 1 -> "год"
            n in 2..4 -> "года"
            else -> "лет"
        }
    return "$n $word назад"
}
