package my.drivebit.utils

import kotlinx.datetime.TimeZone

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
