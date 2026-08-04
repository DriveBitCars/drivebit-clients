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
}
