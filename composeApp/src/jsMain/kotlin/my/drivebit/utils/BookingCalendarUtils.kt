package my.drivebit.utils

import kotlinx.datetime.LocalDate
import my.drivebit.network.services.CarBookingItem

fun parseDisabledDatesFromBookings(carBookings: List<CarBookingItem>): Set<String> {
    val result = mutableSetOf<String>()
    for (booking in carBookings) {
        val startStr = booking.startAt.take(10)
        val endStr = booking.endAt.take(10)
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
