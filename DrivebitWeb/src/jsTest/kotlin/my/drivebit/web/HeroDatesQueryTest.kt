package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeroDatesQueryTest {
    @Test
    fun parseHeroDatesFromQuery_readsStartAndEnd() {
        val (start, end) = parseHeroDatesFromQuery("?startDate=2026-04-25&endDate=2026-04-30")
        assertEquals("2026-04-25", start)
        assertEquals("2026-04-30", end)
    }

    @Test
    fun parseHeroDatesFromQuery_emptySearchReturnsNulls() {
        val (start, end) = parseHeroDatesFromQuery("")
        assertNull(start)
        assertNull(end)
    }

    @Test
    fun parseHeroDatesFromQuery_ignoresBlankValues() {
        val (start, end) = parseHeroDatesFromQuery("?startDate=&endDate=2026-04-30")
        assertNull(start)
        assertEquals("2026-04-30", end)
    }

    @Test
    fun formatHeroDateDisplay_formatsIsoDate() {
        assertEquals("25.04.2026", formatHeroDateDisplay("2026-04-25"))
    }

    @Test
    fun buildHeroSearchUrl_includesQueryParams() {
        assertEquals(
            "/moskva/search?startDate=2026-04-25&endDate=2026-04-30",
            buildHeroSearchUrl("2026-04-25", "2026-04-30"),
        )
    }

    @Test
    fun buildHeroSearchUrl_withoutDates() {
        assertEquals("/moskva/search", buildHeroSearchUrl(null, null))
    }

    @Test
    fun buildHeroSearchUrl_usesCitySlugFromArgument() {
        assertEquals(
            "/kaliningrad/search",
            buildHeroSearchUrl(null, null, citySlug = "kaliningrad"),
        )
    }

    @Test
    fun isSearchPath_matchesSearchRoutes() {
        assertTrue(isSearchPath("/search"))
        assertTrue(isSearchPath("/search/"))
        assertTrue(isSearchPath("/search/bmw"))
        assertTrue(isSearchPath("/audi"))
        assertTrue(isSearchPath("/bmw"))
        assertTrue(isSearchPath("/moskva/search"))
        assertTrue(isSearchPath("/kaliningrad/search/"))
        assertFalse(isSearchPath("/moskva"))
        assertFalse(isSearchPath("/"))
    }

    @Test
    fun isMyCitySelectionPath_matchesMyCitySelection() {
        assertTrue(isMyCitySelectionPath("/my-city-selection"))
        assertTrue(isMyCitySelectionPath("/my-city-selection/"))
        assertFalse(isMyCitySelectionPath("/search"))
        assertFalse(isMyCitySelectionPath("/moskva"))
    }

    @Test
    fun shouldShowStaticHeroShell_onlyOnCityHomeRoutes() {
        assertTrue(shouldShowStaticHeroShell("/"))
        assertTrue(shouldShowStaticHeroShell("/moskva"))
        assertTrue(shouldShowStaticHeroShell("/moskva/poblizosti"))
        assertFalse(shouldShowStaticHeroShell("/search"))
        assertFalse(shouldShowStaticHeroShell("/moskva/search"))
        assertFalse(shouldShowStaticHeroShell("/rostov-na-donu/search"))
        assertFalse(shouldShowStaticHeroShell("/my-city-selection"))
        assertFalse(shouldShowStaticHeroShell("/profile"))
        assertFalse(shouldShowStaticHeroShell("/login-by-phone"))
        assertFalse(shouldShowStaticHeroShell("/my-bookings"))
        assertFalse(shouldShowStaticHeroShell("/payment"))
    }
}
