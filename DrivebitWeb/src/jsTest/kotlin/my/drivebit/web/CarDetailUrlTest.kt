package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarDetailUrlTest {
    @Test
    fun buildCarDetailUrl_includesCarId() {
        val url = buildCarDetailUrl(carId = "abc-123")
        assertEquals("/car-detail?id=abc-123", url)
    }

    @Test
    fun buildCarDetailUrl_includesDatesWhenProvided() {
        val url =
            buildCarDetailUrl(
                carId = "abc-123",
                startDate = "2026-06-10",
                endDate = "2026-06-15",
            )
        assertTrue(url.startsWith("/car-detail?"))
        assertTrue(url.contains("id=abc-123"))
        assertTrue(url.contains("startAt="))
        assertTrue(url.contains("endAt="))
    }
}
