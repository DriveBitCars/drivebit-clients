package my.drivebit.viewmodels

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun mapIso8601ToDateString(iso8601String: String?): String? {
    if (iso8601String == null) return null
    return try {
        val instant = Instant.parse(iso8601String)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year
        "$day.$month.$year"
    } catch (e: Exception) {
        null
    }
}

fun mapIso8601ToTimeString(iso8601String: String?): String? {
    if (iso8601String == null) return null
    return try {
        val instant = Instant.parse(iso8601String)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        "$hour:$minute"
    } catch (e: Exception) {
        null
    }
}
