package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CitySearchPathUtilsTest {
    @Test
    fun parseCitySlugFromSearchPath_readsCitySegment() {
        assertEquals("moskva", parseCitySlugFromSearchPath("/moskva/search"))
        assertEquals("kaliningrad", parseCitySlugFromSearchPath("/kaliningrad/search/"))
        assertEquals("zelenograd", parseCitySlugFromSearchPath("/zelenograd/search?startDate=2026-01-01"))
    }

    @Test
    fun parseCitySlugFromSearchPath_rejectsNonSearchPaths() {
        assertNull(parseCitySlugFromSearchPath("/moskva"))
        assertNull(parseCitySlugFromSearchPath("/moskva/poblizosti"))
        assertNull(parseCitySlugFromSearchPath("/search"))
        assertNull(parseCitySlugFromSearchPath("/audi"))
    }

    @Test
    fun buildCitySearchPath_buildsPathWithOptionalQuery() {
        assertEquals("/moskva/search", buildCitySearchPath("moskva"))
        assertEquals(
            "/kaliningrad/search?startDate=2026-04-25&endDate=2026-04-30",
            buildCitySearchPath("kaliningrad", "startDate=2026-04-25&endDate=2026-04-30"),
        )
    }

    @Test
    fun isCitySearchPath_matchesCitySearchOnly() {
        assertTrue(isCitySearchPath("/moskva/search"))
        assertTrue(isCitySearchPath("/lyubertsy/search/"))
        assertFalse(isCitySearchPath("/search"))
        assertFalse(isCitySearchPath("/moskva"))
        assertFalse(isCitySearchPath("/moskva/poblizosti"))
    }
}
