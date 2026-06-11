@file:OptIn(kotlin.time.ExperimentalTime::class)

package my.drivebit.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class AvailabilityBlockInterval(
    val startAt: String,
    val endAt: String,
)

fun availabilityBlockIntervalFromDates(
    startDate: String,
    endDate: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): AvailabilityBlockInterval {
    val start = LocalDate.parse(startDate.take(10))
    val end = LocalDate.parse(endDate.take(10))
    val startAt =
        LocalDateTime(start.year, start.month, start.day, 0, 0, 0, 0)
            .toInstant(timeZone)
            .toString()
    val endExclusive = addDays(end, 1)
    val endAt =
        LocalDateTime(endExclusive.year, endExclusive.month, endExclusive.day, 0, 0, 0, 0)
            .toInstant(timeZone)
            .toString()
    return AvailabilityBlockInterval(startAt = startAt, endAt = endAt)
}

fun availabilityBlockStartLocalDate(
    startAt: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    val local = Instant.parse(startAt).toLocalDateTime(timeZone)
    return LocalDate(local.year, local.month, local.day)
}

fun availabilityBlockLastLocalDate(
    endAt: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    val local = Instant.parse(endAt).toLocalDateTime(timeZone)
    val date = LocalDate(local.year, local.month, local.day)
    return if (local.hour == 0 && local.minute == 0 && local.second == 0 && local.nanosecond == 0) {
        addDays(date, -1)
    } else {
        date
    }
}

fun formatLocalDate(date: LocalDate): String {
    val day = date.day.toString().padStart(2, '0')
    val month =
        date.month.number
            .toString()
            .padStart(2, '0')
    val year = date.year
    return "$day.$month.$year"
}

fun formatAvailabilityBlockPeriod(
    startAt: String,
    endAt: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val startDisplay = formatLocalDate(availabilityBlockStartLocalDate(startAt, timeZone))
    val endDisplay = formatLocalDate(availabilityBlockLastLocalDate(endAt, timeZone))
    return if (startDisplay == endDisplay) startDisplay else "$startDisplay — $endDisplay"
}
