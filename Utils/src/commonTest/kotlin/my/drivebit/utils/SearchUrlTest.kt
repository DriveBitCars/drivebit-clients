package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SearchUrlTest {
    @Test
    fun `parse brand and model from search path`() {
        val parsed = parseSearchUrl("/search/bmw/x5")
        assertEquals("bmw", parsed.brandSlug)
        assertEquals("x5", parsed.modelSlug)
        assertNull(parsed.citySlug)
    }

    @Test
    fun `parse brand and model from short brand path`() {
        val parsed = parseSearchUrl("/bmw/x5")
        assertEquals("bmw", parsed.brandSlug)
        assertEquals("x5", parsed.modelSlug)
    }

    @Test
    fun `parse city search path without brand`() {
        val parsed = parseSearchUrl("/moskva/search")
        assertEquals("moskva", parsed.citySlug)
        assertNull(parsed.brandSlug)
        assertNull(parsed.modelSlug)
    }

    @Test
    fun `parse query filters and page`() {
        val parsed =
            parseSearchUrl(
                "/moskva/search?startDate=2025-02-01&endDate=2025-02-15&dailyRateMin=1000&dailyRateMax=5000" +
                    "&driveType=awd&driveTypeLabel=Полный&bodyType=suv&bodyTypeLabel=Внедорожник" +
                    "&seatsMin=5&yearMin=2020&yearMax=2024&mileageMin=200&page=2",
            )
        assertEquals("2025-02-01", parsed.startDate)
        assertEquals("2025-02-15", parsed.endDate)
        assertEquals(1000, parsed.dailyRateMin)
        assertEquals(5000, parsed.dailyRateMax)
        assertEquals("awd", parsed.driveType)
        assertEquals("Полный", parsed.driveTypeLabel)
        assertEquals("suv", parsed.bodyType)
        assertEquals("Внедорожник", parsed.bodyTypeLabel)
        assertEquals(5, parsed.seatsMin)
        assertEquals(2020, parsed.yearMin)
        assertEquals(2024, parsed.yearMax)
        assertEquals(200, parsed.mileageMin)
        assertEquals(2, parsed.page)
    }

    @Test
    fun `build uses search prefix for all brand slugs including bmw and audi`() {
        assertEquals("/search/bmw", buildSearchUrl(SearchUrlParts(brandSlug = "bmw")))
        assertEquals("/search/audi", buildSearchUrl(SearchUrlParts(brandSlug = "audi")))
        assertEquals("/search/bmw/x5", buildSearchUrl(SearchUrlParts(brandSlug = "bmw", modelSlug = "x5")))
        assertEquals("/search/audi/a6", buildSearchUrl(SearchUrlParts(brandSlug = "audi", modelSlug = "a6")))
        assertEquals("/search/toyota", buildSearchUrl(SearchUrlParts(brandSlug = "toyota")))
        assertEquals(
            "/search/skoda/octavia",
            buildSearchUrl(SearchUrlParts(brandSlug = "skoda", modelSlug = "octavia")),
        )
    }

    @Test
    fun `build omits nulls and page 1 and never puts brand or model in query`() {
        val url =
            buildSearchUrl(
                SearchUrlParts(
                    citySlug = "moskva",
                    brandSlug = "bmw",
                    modelSlug = "x5",
                    startDate = "2025-02-01",
                    dailyRateMin = 1000,
                    page = 1,
                ),
            )
        assertEquals("/search/bmw/x5?startDate=2025-02-01&dailyRateMin=1000", url)
        assertFalse(url.contains("brand"))
        assertFalse(url.contains("model"))
        assertFalse(url.contains("page="))
    }

    @Test
    fun `round trip city search with filters`() {
        val original =
            SearchUrlParts(
                citySlug = "kaliningrad",
                startDate = "2025-03-01",
                endDate = "2025-03-10",
                seatsMin = 4,
                page = 3,
            )
        val url = buildSearchUrl(original)
        val parsed = parseSearchUrl(url)
        assertEquals(original.citySlug, parsed.citySlug)
        assertEquals(original.startDate, parsed.startDate)
        assertEquals(original.endDate, parsed.endDate)
        assertEquals(original.seatsMin, parsed.seatsMin)
        assertEquals(original.page, parsed.page)
        assertNull(parsed.brandSlug)
        assertNull(parsed.modelSlug)
    }

    @Test
    fun `parseBrandSlug still works for brand-only path`() {
        assertEquals("bmw", parseBrandSlugFromPath("/search/bmw"))
        assertEquals("bmw", parseSearchUrl("/search/bmw").brandSlug)
        assertNull(parseSearchUrl("/search/bmw").modelSlug)
    }

    @Test
    fun `parse accepts both short and search-prefixed bmw paths`() {
        assertEquals("bmw", parseSearchUrl("/bmw").brandSlug)
        assertEquals("bmw", parseSearchUrl("/search/bmw").brandSlug)
        assertEquals("toyota", parseSearchUrl("/search/toyota").brandSlug)
        assertNull(parseSearchUrl("/toyota").brandSlug)
    }

    @Test
    fun `build includes page when greater than 1`() {
        val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 2))
        assertEquals("/moskva/search?page=2", url)
    }

    @Test
    fun `build omits page query when page is 1`() {
        val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 1))
        assertEquals("/moskva/search", url)
        assertFalse(url.contains("page="))
    }

    @Test
    fun `round trip preserves page 3 on city search`() {
        val url = buildSearchUrl(SearchUrlParts(citySlug = "moskva", page = 3))
        assertEquals("/moskva/search?page=3", url)
        assertEquals(3, parseSearchUrl(url).page)
        assertEquals(url, buildSearchUrl(parseSearchUrl(url)))
    }

    @Test
    fun `parse invalid or missing page becomes 1`() {
        assertEquals(1, parseSearchUrl("/moskva/search").page)
        assertEquals(1, parseSearchUrl("/moskva/search?page=0").page)
        assertEquals(1, parseSearchUrl("/moskva/search?page=-2").page)
        assertEquals(1, parseSearchUrl("/moskva/search?page=abc").page)
    }
}
