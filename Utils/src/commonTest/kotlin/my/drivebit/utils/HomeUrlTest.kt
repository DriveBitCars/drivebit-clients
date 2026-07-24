package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class HomeUrlTest {
    @Test
    fun `parse city root without filter`() {
        val parsed = parseHomeUrl("/moskva")
        assertEquals("moskva", parsed.citySlug)
        assertNull(parsed.filterSlug)
        assertEquals(1, parsed.page)
        assertNull(parsed.startDate)
        assertNull(parsed.endDate)
        assertNull(parsed.lat)
        assertNull(parsed.lon)
        assertNull(parsed.radiusKm)
    }

    @Test
    fun `parse city with filter slug`() {
        val parsed = parseHomeUrl("/moskva/poblizosti")
        assertEquals("moskva", parsed.citySlug)
        assertEquals("poblizosti", parsed.filterSlug)
    }

    @Test
    fun `parse dates page and nearby geo from query`() {
        val parsed =
            parseHomeUrl(
                "/moskva/poblizosti?startDate=2026-04-25&endDate=2026-04-30&page=2" +
                    "&lat=55.75&lon=37.62&radiusKm=20",
            )
        assertEquals("2026-04-25", parsed.startDate)
        assertEquals("2026-04-30", parsed.endDate)
        assertEquals(2, parsed.page)
        assertEquals(55.75, parsed.lat)
        assertEquals(37.62, parsed.lon)
        assertEquals(20, parsed.radiusKm)
    }

    @Test
    fun `parse invalid or missing page becomes 1`() {
        assertEquals(1, parseHomeUrl("/moskva").page)
        assertEquals(1, parseHomeUrl("/moskva?page=0").page)
        assertEquals(1, parseHomeUrl("/moskva?page=-2").page)
        assertEquals(1, parseHomeUrl("/moskva?page=abc").page)
    }

    @Test
    fun `build city root omits defaults`() {
        val url = buildHomeUrl(HomeUrlParts(citySlug = "moskva"))
        assertEquals("/moskva", url)
        assertFalse(url.contains("page="))
        assertFalse(url.contains("radiusKm="))
    }

    @Test
    fun `build includes filter slug and query when set`() {
        val url =
            buildHomeUrl(
                HomeUrlParts(
                    citySlug = "moskva",
                    filterSlug = "poblizosti",
                    startDate = "2026-04-25",
                    endDate = "2026-04-30",
                    page = 2,
                    lat = 55.75,
                    lon = 37.62,
                    radiusKm = 20,
                ),
            )
        assertEquals(
            "/moskva/poblizosti?startDate=2026-04-25&endDate=2026-04-30&page=2" +
                "&lat=55.75&lon=37.62&radiusKm=20",
            url,
        )
    }

    @Test
    fun `build omits page 1 and default radiusKm 10`() {
        val url =
            buildHomeUrl(
                HomeUrlParts(
                    citySlug = "moskva",
                    filterSlug = "poblizosti",
                    page = 1,
                    lat = 55.75,
                    lon = 37.62,
                    radiusKm = 10,
                ),
            )
        assertEquals("/moskva/poblizosti?lat=55.75&lon=37.62", url)
        assertFalse(url.contains("page="))
        assertFalse(url.contains("radiusKm="))
    }

    @Test
    fun `round trip preserves home url parts`() {
        val original =
            HomeUrlParts(
                citySlug = "moskva",
                filterSlug = "arenda-avto-v-krym",
                startDate = "2026-04-25",
                endDate = "2026-04-30",
                page = 3,
                lat = 55.751244,
                lon = 37.618423,
                radiusKm = 15,
            )
        val parsed = parseHomeUrl(buildHomeUrl(original))
        assertEquals(original.citySlug, parsed.citySlug)
        assertEquals(original.filterSlug, parsed.filterSlug)
        assertEquals(original.startDate, parsed.startDate)
        assertEquals(original.endDate, parsed.endDate)
        assertEquals(original.page, parsed.page)
        assertEquals(original.lat, parsed.lat)
        assertEquals(original.lon, parsed.lon)
        assertEquals(original.radiusKm, parsed.radiusKm)
    }

    @Test
    fun `parse excludes search path as home filter`() {
        val parsed = parseHomeUrl("/moskva/search?page=2")
        assertEquals("moskva", parsed.citySlug)
        assertNull(parsed.filterSlug)
        assertEquals(2, parsed.page)
    }
}
