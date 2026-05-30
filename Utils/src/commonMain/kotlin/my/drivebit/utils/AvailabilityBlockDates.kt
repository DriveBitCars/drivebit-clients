@file:OptIn(kotlin.time.ExperimentalTime::class)

package my.drivebit.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

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

fun formatAvailabilityBlockPeriod(
    startAt: String,
    endAt: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val startDisplay = mapIso8601ToDateString(startAt)
    val endExclusive = LocalDate.parse(endAt.take(10))
    val lastBlockedDay = addDays(endExclusive, -1)
    val endDisplay =
        mapIso8601ToDateString(
            LocalDateTime(lastBlockedDay.year, lastBlockedDay.month, lastBlockedDay.day, 0, 0, 0, 0)
                .toInstant(timeZone)
                .toString(),
        )
    return if (startDisplay == endDisplay) startDisplay else "$startDisplay — $endDisplay"
}
