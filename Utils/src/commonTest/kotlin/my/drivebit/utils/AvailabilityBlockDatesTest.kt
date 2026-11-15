package my.drivebit.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityBlockDatesTest {
    private val utcPlus5 = TimeZone.of("+05:00")

    @Test
    fun `availabilityBlockIntervalFromDates should use start of day and exclusive end`() {
        val interval =
            availabilityBlockIntervalFromDates(
                startDate = "2026-06-10",
                endDate = "2026-06-12",
                timeZone = TimeZone.UTC,
            )

        assertTrue(interval.startAt.startsWith("2026-06-10T00:00:00"))
        assertTrue(interval.endAt.startsWith("2026-06-13T00:00:00"))
    }

    @Test
    fun `availabilityBlockIntervalFromDates single day should have end next day`() {
        val interval =
            availabilityBlockIntervalFromDates(
                startDate = "2026-06-10",
                endDate = "2026-06-10",
                timeZone = TimeZone.UTC,
            )

        assertTrue(interval.startAt.startsWith("2026-06-10T00:00:00"))
        assertTrue(interval.endAt.startsWith("2026-06-11T00:00:00"))
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show single day without dash`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-10T00:00:00Z",
                endAt = "2026-06-11T00:00:00Z",
                timeZone = TimeZone.UTC,
            )

        assertEquals("10.06.2026", period)
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show range`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-10T00:00:00Z",
                endAt = "2026-06-13T00:00:00Z",
                timeZone = TimeZone.UTC,
            )

        assertEquals("10.06.2026 — 12.06.2026", period)
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show API inclusive end range`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-22T00:00:00.000Z",
                endAt = "2026-06-23T23:59:59.000Z",
                timeZone = TimeZone.UTC,
            )

        assertEquals("22.06.2026 — 23.06.2026", period)
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show API inclusive end range for 25-26`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-25T00:00:00.000Z",
                endAt = "2026-06-26T23:59:59.000Z",
                timeZone = TimeZone.UTC,
            )

        assertEquals("25.06.2026 — 26.06.2026", period)
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show single day for exclusive midnight UTC+5`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-19T19:00:00Z",
                endAt = "2026-06-20T19:00:00Z",
                timeZone = utcPlus5,
            )

        assertEquals("20.06.2026", period)
    }

    @Test
    fun `formatAvailabilityBlockPeriod should show range for exclusive midnight UTC+5`() {
        val period =
            formatAvailabilityBlockPeriod(
                startAt = "2026-06-10T19:00:00Z",
                endAt = "2026-06-12T19:00:00Z",
                timeZone = utcPlus5,
            )

        assertEquals("11.06.2026 — 12.06.2026", period)
    }

    @Test
    fun `parseDisabledDatesFromIsoIntervals should match API inclusive end`() {
        val disabled =
            parseDisabledDatesFromIsoIntervals(
                listOf("2026-06-22T00:00:00.000Z" to "2026-06-23T23:59:59.000Z"),
                timeZone = TimeZone.UTC,
            )

        assertEquals(setOf("2026-06-22", "2026-06-23"), disabled)
    }

    @Test
    fun `parseDisabledDatesFromIsoIntervals should match exclusive midnight UTC+5`() {
        val disabled =
            parseDisabledDatesFromIsoIntervals(
                listOf("2026-06-10T19:00:00Z" to "2026-06-12T19:00:00Z"),
                timeZone = utcPlus5,
            )

        assertEquals(setOf("2026-06-11", "2026-06-12"), disabled)
    }

    @Test
    fun `parseDisabledDatesFromIsoIntervals should use exclusive end for midnight UTC`() {
        val disabled =
            parseDisabledDatesFromIsoIntervals(
                listOf("2026-06-10T00:00:00Z" to "2026-06-12T00:00:00Z"),
                timeZone = TimeZone.UTC,
            )

        assertEquals(setOf("2026-06-10", "2026-06-11"), disabled)
    }

    @Test
    fun `parseFullyOccupiedDisabledDates keeps partial first and last day selectable`() {
        val disabled =
            parseFullyOccupiedDisabledDatesFromIsoIntervals(
                listOf("2026-03-10T10:00:00Z" to "2026-03-12T18:00:00Z"),
                timeZone = TimeZone.UTC,
            )

        assertEquals(setOf("2026-03-11"), disabled)
    }

    @Test
    fun `parseFullyOccupiedDisabledDates disables full local days for exclusive midnight block`() {
        val disabled =
            parseFullyOccupiedDisabledDatesFromIsoIntervals(
                listOf("2026-06-10T00:00:00Z" to "2026-06-13T00:00:00Z"),
                timeZone = TimeZone.UTC,
            )

        assertEquals(setOf("2026-06-10", "2026-06-11", "2026-06-12"), disabled)
    }

    @Test
    fun `dateRangeContainsDisabledDate detects interior busy days`() {
        assertTrue(
            dateRangeContainsDisabledDate(
                start = LocalDate.parse("2026-06-10"),
                end = LocalDate.parse("2026-06-25"),
                disabledDates = setOf("2026-06-15", "2026-06-16"),
            ),
        )
        assertTrue(
            !dateRangeContainsDisabledDate(
                start = LocalDate.parse("2026-06-10"),
                end = LocalDate.parse("2026-06-12"),
                disabledDates = setOf("2026-06-15"),
            ),
        )
    }

    @Test
    fun `availableStartHourOptions hides hours inside occupancy`() {
        val occupancy =
            parseOccupancyIntervalsEpochMs(
                listOf("2026-06-10T15:00:00Z" to "2026-06-10T20:00:00Z"),
            )
        val hours = (0..23).map { "${it.toString().padStart(2, '0')}:00" }
        val available =
            availableStartHourOptions(
                dateIso = "2026-06-10",
                allHours = hours,
                occupancy = occupancy,
                timeZone = TimeZone.UTC,
            )
        assertTrue("14:00" in available)
        assertTrue("15:00" !in available)
        assertTrue("19:00" !in available)
        assertTrue("20:00" in available)
    }

    @Test
    fun `availableEndHourOptions hides hours that would overlap occupancy`() {
        val occupancy =
            parseOccupancyIntervalsEpochMs(
                listOf("2026-06-11T18:00:00Z" to "2026-06-18T18:00:00Z"),
            )
        val hours = (0..23).map { "${it.toString().padStart(2, '0')}:00" }
        val available =
            availableEndHourOptions(
                dateIso = "2026-06-11",
                startAtIso = "2026-06-10T12:00:00Z",
                allHours = hours,
                occupancy = occupancy,
                timeZone = TimeZone.UTC,
            )
        assertTrue("12:00" in available)
        assertTrue("18:00" in available)
        assertTrue("19:00" !in available)
    }

    @Test
    fun `availability block local dates should keep start before end`() {
        val intervals =
            listOf(
                "2026-06-22T00:00:00.000Z" to "2026-06-23T23:59:59.000Z",
                "2026-06-25T00:00:00.000Z" to "2026-06-26T23:59:59.000Z",
                "2026-06-19T19:00:00Z" to "2026-06-20T19:00:00Z",
                "2026-06-10T19:00:00Z" to "2026-06-12T19:00:00Z",
            )
        for ((startAt, endAt) in intervals) {
            val timeZone = if (startAt.contains("19:00:00")) utcPlus5 else TimeZone.UTC
            val start = availabilityBlockStartLocalDate(startAt, timeZone)
            val end = availabilityBlockLastLocalDate(endAt, timeZone)
            assertTrue(start <= end, "start $start should not be after end $end for $startAt / $endAt")
        }
    }
}
