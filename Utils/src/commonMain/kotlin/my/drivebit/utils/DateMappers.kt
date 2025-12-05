package my.drivebit.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

private val monthNames =
    listOf(
        "январь",
        "февраль",
        "март",
        "апрель",
        "май",
        "июнь",
        "июль",
        "август",
        "сентябрь",
        "октябрь",
        "ноябрь",
        "декабрь",
    )

fun mapIso8601ToDateString(iso8601String: String): String {
    val instant = Instant.parse(iso8601String)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = localDateTime.day.toString().padStart(2, '0')
    val month =
        localDateTime.month.number
            .toString()
            .padStart(2, '0')
    val year = localDateTime.year
    return "$day.$month.$year"
}

fun mapIso8601ToTimeString(iso8601String: String): String {
    val instant = Instant.parse(iso8601String)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun mapIso8601ToMonthYearString(iso8601String: String): String {
    val instant = Instant.parse(iso8601String)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthIndex = localDateTime.month.number - 1
    val monthName = monthNames.getOrNull(monthIndex)!!
    val year = localDateTime.year
    return "$monthName $year"
}
