package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BookingFunnelAnalyticsTest {
    @Test
    fun goals_haveStableIdentifiersForMetrika() {
        assertEquals("bron", BookingFunnelGoals.LEGACY)
        assertEquals("bron_click", BookingFunnelGoals.CLICK)
        assertEquals("bron_auto", BookingFunnelGoals.AUTO)
        assertEquals("bron_create_ok", BookingFunnelGoals.CREATE_OK)
        assertEquals("bron_create_fail", BookingFunnelGoals.CREATE_FAIL)
    }

    @Test
    fun intentParams_includeSourceAndCarId() {
        val params =
            bookingFunnelIntentParams(
                source = BookingFunnelSource.Click,
                carId = "a2dea523-56d7-47df-81d8-cd064d67f830",
            )
        assertEquals("click", params["source"])
        assertEquals("a2dea523-56d7-47df-81d8-cd064d67f830", params["carId"])
        assertFalse(params.containsKey("bookingId"))
    }

    @Test
    fun createOkParams_includeBookingId() {
        val params =
            bookingFunnelCreateParams(
                source = BookingFunnelSource.Auto,
                carId = "car-1",
                bookingId = "c1a55dd4-fafc-4e89-9dd4-07c2e3036d89",
            )
        assertEquals("auto", params["source"])
        assertEquals("car-1", params["carId"])
        assertEquals("c1a55dd4-fafc-4e89-9dd4-07c2e3036d89", params["bookingId"])
    }

    @Test
    fun createFailParams_truncateLongMessage() {
        val long = "x".repeat(300)
        val params =
            bookingFunnelCreateParams(
                source = BookingFunnelSource.Click,
                carId = "car-1",
                bookingId = null,
                message = long,
            )
        assertEquals(120, params.getValue("message").length)
        assertFalse(params.containsKey("bookingId"))
    }
}
