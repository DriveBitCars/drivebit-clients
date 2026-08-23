package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SeasonalPriceAdjustmentTest {
    @Test
    fun `applySeasonalAdjustment adds percent markup`() {
        assertEquals(1100.0, applySeasonalAdjustment(1000.0, 10.0))
    }

    @Test
    fun `applySeasonalAdjustment applies percent discount`() {
        assertEquals(800.0, applySeasonalAdjustment(1000.0, -20.0))
    }

    @Test
    fun `applySeasonalAdjustment returns same rate when percent is zero`() {
        assertEquals(1000.0, applySeasonalAdjustment(1000.0, 0.0))
    }

    @Test
    fun `removeSeasonalAdjustment reverses markup to base rate`() {
        assertEquals(1000.0, removeSeasonalAdjustment(1100.0, 10.0))
    }

    @Test
    fun `removeSeasonalAdjustment reverses discount to base rate`() {
        assertEquals(1000.0, removeSeasonalAdjustment(800.0, -20.0))
    }

    @Test
    fun `removeSeasonalAdjustment returns same rate when percent is zero`() {
        assertEquals(1100.0, removeSeasonalAdjustment(1100.0, 0.0))
    }

    @Test
    fun `clampSeasonalPercent clamps below minimum to -90`() {
        assertEquals(-90.0, clampSeasonalPercent(-100.0))
    }

    @Test
    fun `clampSeasonalPercent clamps above maximum to 1000`() {
        assertEquals(1000.0, clampSeasonalPercent(1500.0))
    }

    @Test
    fun `parseSeasonalPercentInput accepts signed integer in range`() {
        assertEquals(15, parseSeasonalPercentInput("15"))
        assertEquals(-20, parseSeasonalPercentInput("-20"))
        assertEquals(0, parseSeasonalPercentInput("0"))
    }

    @Test
    fun `parseSeasonalPercentInput rejects blank and out of range`() {
        assertNull(parseSeasonalPercentInput(""))
        assertNull(parseSeasonalPercentInput(" "))
        assertNull(parseSeasonalPercentInput("abc"))
        assertNull(parseSeasonalPercentInput("-91"))
        assertNull(parseSeasonalPercentInput("1001"))
    }

    @Test
    fun `isoDateTimeToLocalDate keeps calendar date`() {
        assertEquals("2026-12-01", isoDateTimeToLocalDate("2026-12-01T00:00:00Z"))
        assertEquals("2026-07-15", isoDateTimeToLocalDate("2026-07-15T23:59:59.000Z"))
        assertEquals("2026-01-02", isoDateTimeToLocalDate("2026-01-02"))
    }

    @Test
    fun `localDateToIsoDateTime sends midnight UTC`() {
        assertEquals("2026-12-01T00:00:00.000Z", localDateToIsoDateTime("2026-12-01"))
    }

    @Test
    fun `validateSeasonalPriceAdjustmentPeriod requires dates`() {
        assertEquals(
            "Укажите даты периода",
            validateSeasonalPriceAdjustmentPeriod(startsAt = "", endsAt = "2026-12-10", percent = "10"),
        )
        assertEquals(
            "Укажите даты периода",
            validateSeasonalPriceAdjustmentPeriod(startsAt = "2026-12-01", endsAt = "", percent = "10"),
        )
    }

    @Test
    fun `validateSeasonalPriceAdjustmentPeriod rejects end before start`() {
        assertEquals(
            "Дата окончания не может быть раньше даты начала",
            validateSeasonalPriceAdjustmentPeriod(
                startsAt = "2026-12-10",
                endsAt = "2026-12-01",
                percent = "10",
            ),
        )
    }

    @Test
    fun `validateSeasonalPriceAdjustmentPeriod rejects invalid percent`() {
        assertEquals(
            "Процент от -90 до 1000",
            validateSeasonalPriceAdjustmentPeriod(
                startsAt = "2026-12-01",
                endsAt = "2026-12-10",
                percent = "abc",
            ),
        )
    }

    @Test
    fun `validateSeasonalPriceAdjustmentPeriod accepts valid period`() {
        assertNull(
            validateSeasonalPriceAdjustmentPeriod(
                startsAt = "2026-12-01",
                endsAt = "2026-12-10",
                percent = "-15",
            ),
        )
    }
}
