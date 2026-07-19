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

    @Test
    fun resolveBareSearchRedirectPath_usesSelectedCityFromRepository() {
        assertEquals("/kaliningrad/search", resolveBareSearchRedirectPath("Калининград"))
        assertEquals("/rostov-na-donu/search", resolveBareSearchRedirectPath("Ростов-на-Дону"))
        assertEquals("/moskva/search", resolveBareSearchRedirectPath("Москва"))
    }

    @Test
    fun resolveBareSearchRedirectPath_fallsBackToMoskvaWhenCityMissing() {
        assertEquals("/moskva/search", resolveBareSearchRedirectPath(null))
        assertEquals("/moskva/search", resolveBareSearchRedirectPath(""))
        assertEquals("/moskva/search", resolveBareSearchRedirectPath("   "))
        assertEquals("/moskva/search", resolveBareSearchRedirectPath("@@@"))
    }

    @Test
    fun resolveBareSearchRedirectPath_preservesQuery() {
        assertEquals(
            "/kaliningrad/search?startDate=2026-04-25&endDate=2026-04-30",
            resolveBareSearchRedirectPath(
                "Калининград",
                "startDate=2026-04-25&endDate=2026-04-30",
            ),
        )
        assertEquals(
            "/moskva/search?page=2",
            resolveBareSearchRedirectPath(null, "page=2"),
        )
    }
}
