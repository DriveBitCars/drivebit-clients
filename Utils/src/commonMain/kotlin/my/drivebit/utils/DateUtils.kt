package my.drivebit.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number

fun addDays(
    date: LocalDate,
    days: Int,
): LocalDate = LocalDate.fromEpochDays(date.toEpochDays() + days)

fun addMonths(
    date: LocalDate,
    months: Int,
): LocalDate {
    var newMonth = date.month.number + months
    var newYear = date.year
    while (newMonth > 12) {
        newMonth -= 12
        newYear += 1
    }
    while (newMonth < 1) {
        newMonth += 12
        newYear -= 1
    }
    val monthEnum = Month.entries[newMonth - 1]
    val day = date.day.coerceAtMost(lastDayOfMonth(newYear, monthEnum))
    return LocalDate(newYear, monthEnum, day)
}

fun lastDayOfMonth(
    year: Int,
    month: Month,
): Int =
    when (month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    }
