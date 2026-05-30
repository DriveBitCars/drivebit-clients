package my.drivebit.utils

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityBlockDatesTest {
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
}
