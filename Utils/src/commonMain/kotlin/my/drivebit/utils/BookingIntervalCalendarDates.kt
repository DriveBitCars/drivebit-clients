@file:OptIn(kotlin.time.ExperimentalTime::class)

package my.drivebit.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Все локальные календарные дни, которые пересекаются с интервалом (включая частично занятые).
 * Для календаря владельца (блокировки целиком по дням).
 */
fun parseDisabledDatesFromIsoIntervals(
    intervals: List<Pair<String, String>>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Set<String> {
    val result = mutableSetOf<String>()
    for ((startRaw, endRaw) in intervals) {
        runCatching {
            var d = availabilityBlockStartLocalDate(startRaw, timeZone)
            val end = availabilityBlockLastLocalDate(endRaw, timeZone)
            while (d <= end) {
                result.add(d.toString())
                d = addDays(d, 1)
            }
        }
    }
    return result
}

/**
 * Только полностью занятые локальные сутки [00:00, next 00:00).
 * Частично занятый первый/последний день брони остаётся выбираемым —
 * конфликт по часам ловит /Booking/calculate.
 */
fun parseFullyOccupiedDisabledDatesFromIsoIntervals(
    intervals: List<Pair<String, String>>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Set<String> {
    val parsed =
        intervals.mapNotNull { (startRaw, endRaw) ->
            runCatching {
                Instant.parse(startRaw).toEpochMilliseconds() to
                    Instant.parse(endRaw).toEpochMilliseconds()
            }.getOrNull()?.takeIf { (start, end) -> start < end }
        }
    if (parsed.isEmpty()) return emptySet()

    val candidateDays = linkedSetOf<LocalDate>()
    for ((startMs, endMs) in parsed) {
        val start = Instant.fromEpochMilliseconds(startMs)
        val end = Instant.fromEpochMilliseconds(endMs)
        var d = start.toLocalDateTime(timeZone).date
        val endLocal = end.toLocalDateTime(timeZone)
        val last =
            if (endLocal.hour == 0 && endLocal.minute == 0 && endLocal.second == 0 && endLocal.nanosecond == 0) {
                addDays(endLocal.date, -1)
            } else {
                endLocal.date
            }
        while (d <= last) {
            candidateDays.add(d)
            d = addDays(d, 1)
        }
    }

    return candidateDays
        .filter { day -> isLocalDayFullyOccupied(day, parsed, timeZone) }
        .map { it.toString() }
        .toSet()
}

fun dateRangeContainsDisabledDate(
    start: LocalDate,
    end: LocalDate,
    disabledDates: Set<String>,
): Boolean {
    if (start >= end) return false
    var d = addDays(start, 1)
    while (d < end) {
        if (d.toString() in disabledDates) return true
        d = addDays(d, 1)
    }
    return false
}

/**
 * Парсит ISO-интервалы занятости в epoch ms [start, end).
 */
fun parseOccupancyIntervalsEpochMs(
    intervals: List<Pair<String, String>>,
): List<Pair<Long, Long>> =
    intervals.mapNotNull { (startRaw, endRaw) ->
        runCatching {
            Instant.parse(startRaw).toEpochMilliseconds() to
                Instant.parse(endRaw).toEpochMilliseconds()
        }.getOrNull()?.takeIf { (start, end) -> start < end }
    }

fun isInstantOccupied(
    instantMs: Long,
    occupancy: List<Pair<Long, Long>>,
): Boolean = occupancy.any { (start, end) -> start <= instantMs && instantMs < end }

/**
 * Та же формула, что PeriodOverlap на бэкенде (для [start, end)).
 */
fun periodsOverlap(
    start1: Long,
    end1: Long,
    start2: Long,
    end2: Long,
): Boolean =
    (start1 <= start2 && end1 > start2) ||
        (start1 < end2 && end1 >= end2) ||
        (start2 <= start1 && end2 >= end1)

fun periodOverlapsOccupancy(
    startMs: Long,
    endMs: Long,
    occupancy: List<Pair<Long, Long>>,
): Boolean {
    if (startMs >= endMs) return true
    return occupancy.any { (busyStart, busyEnd) ->
        periodsOverlap(startMs, endMs, busyStart, busyEnd)
    }
}

/**
 * Часы, в которые можно начать аренду в этот день (момент не внутри занятости).
 * [minExclusiveHour] — для «сегодня»: только часы строго позже текущего.
 */
fun availableStartHourOptions(
    dateIso: String,
    allHours: List<String>,
    occupancy: List<Pair<Long, Long>>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    minExclusiveHour: Int? = null,
): List<String> {
    val date = runCatching { LocalDate.parse(dateIso.take(10)) }.getOrNull() ?: return allHours
    return allHours.filter { hourValue ->
        val hour = hourOptionToHour(hourValue)
        if (minExclusiveHour != null && hour <= minExclusiveHour) return@filter false
        val ms = localDateAndHourToEpochMs(date, hourValue, timeZone)
        !isInstantOccupied(ms, occupancy)
    }
}

/**
 * Часы окончания аренды: момент после start и интервал [start, end) без пересечений с занятостью.
 */
fun availableEndHourOptions(
    dateIso: String,
    startAtIso: String,
    allHours: List<String>,
    occupancy: List<Pair<Long, Long>>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): List<String> {
    val date = runCatching { LocalDate.parse(dateIso.take(10)) }.getOrNull() ?: return emptyList()
    val startMs =
        runCatching { Instant.parse(startAtIso).toEpochMilliseconds() }.getOrNull()
            ?: return emptyList()
    return allHours.filter { hourValue ->
        val endMs = localDateAndHourToEpochMs(date, hourValue, timeZone)
        endMs > startMs && !periodOverlapsOccupancy(startMs, endMs, occupancy)
    }
}

fun hourOptionToHour(value: String): Int =
    when (value.trim()) {
        "24:00" -> 24
        else -> value.substringBefore(':').toIntOrNull() ?: -1
    }

fun localDateAndHourToEpochMs(
    date: LocalDate,
    hourValue: String,
    timeZone: TimeZone,
): Long {
    val t = hourValue.trim()
    val local =
        if (t == "24:00" || t.startsWith("24:")) {
            LocalDateTime(date.year, date.month, date.day, 23, 59, 0, 0)
        } else {
            val parts = t.split(':')
            val hh = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val mm = parts.getOrNull(1)?.toIntOrNull() ?: 0
            LocalDateTime(date.year, date.month, date.day, hh.coerceIn(0, 23), mm.coerceIn(0, 59), 0, 0)
        }
    return local.toInstant(timeZone).toEpochMilliseconds()
}

internal fun isLocalDayFullyOccupied(
    day: LocalDate,
    intervals: List<Pair<Long, Long>>,
    timeZone: TimeZone,
): Boolean {
    val dayStartMs =
        LocalDateTime(day.year, day.month, day.day, 0, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()
    val next = addDays(day, 1)
    val dayEndMs =
        LocalDateTime(next.year, next.month, next.day, 0, 0, 0, 0)
            .toInstant(timeZone)
            .toEpochMilliseconds()

    val pieces =
        intervals
            .mapNotNull { (start, end) ->
                val a = maxOf(start, dayStartMs)
                val b = minOf(end, dayEndMs)
                if (a < b) a to b else null
            }.sortedBy { it.first }

    if (pieces.isEmpty()) return false

    var coverUntil = dayStartMs
    for ((a, b) in pieces) {
        if (a > coverUntil) return false
        if (b > coverUntil) coverUntil = b
    }
    return coverUntil >= dayEndMs
}
