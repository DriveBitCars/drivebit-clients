package my.drivebit.utils

import kotlinx.datetime.LocalDate

fun parseDisabledDatesFromIsoIntervals(intervals: List<Pair<String, String>>): Set<String> {
    val result = mutableSetOf<String>()
    for ((startRaw, endRaw) in intervals) {
        val startStr = startRaw.take(10)
        val endStr = endRaw.take(10)
        if (startStr.length != 10 || endStr.length != 10) continue
        runCatching {
            var d = LocalDate.parse(startStr)
            val end = LocalDate.parse(endStr)
            while (d <= end) {
                result.add(d.toString())
                d = addDays(d, 1)
            }
        }
    }
    return result
}
