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

    @Test
    fun `parse body type path without brand`() {
        val parsed = parseSearchUrl("/search/sedan")
        assertNull(parsed.brandSlug)
        assertNull(parsed.modelSlug)
        assertNull(parsed.citySlug)
        assertEquals("Sedan", parsed.bodyType)
        assertEquals("Седан", parsed.bodyTypeLabel)
    }

    @Test
    fun `parse suv body path`() {
        val parsed = parseSearchUrl("/search/suv")
        assertEquals("SUV", parsed.bodyType)
        assertEquals("Внедорожник", parsed.bodyTypeLabel)
        assertNull(parsed.brandSlug)
    }

    @Test
    fun `build body-only path omits body query keys`() {
        val url =
            buildSearchUrl(
                SearchUrlParts(
                    bodyType = "Sedan",
                    bodyTypeLabel = "Седан",
                ),
            )
        assertEquals("/search/sedan", url)
        assertFalse(url.contains("bodyType="))
    }

    @Test
    fun `build prefers body path over city when brand missing`() {
        val url =
            buildSearchUrl(
                SearchUrlParts(
                    citySlug = "moskva",
                    bodyType = "Sedan",
                    bodyTypeLabel = "Седан",
                ),
            )
        assertEquals("/search/sedan", url)
    }

    @Test
    fun `build brand with body keeps body in query`() {
        val url =
            buildSearchUrl(
                SearchUrlParts(
                    brandSlug = "bmw",
                    modelSlug = "x5",
                    bodyType = "SUV",
                    bodyTypeLabel = "Внедорожник",
                ),
            )
        assertEquals("/search/bmw/x5?bodyType=SUV&bodyTypeLabel=%D0%92%D0%BD%D0%B5%D0%B4%D0%BE%D1%80%D0%BE%D0%B6%D0%BD%D0%B8%D0%BA", url)
        assertNull(parseSearchUrl(url).citySlug)
        assertEquals("bmw", parseSearchUrl(url).brandSlug)
        assertEquals("x5", parseSearchUrl(url).modelSlug)
        assertEquals("SUV", parseSearchUrl(url).bodyType)
    }

    @Test
    fun `city search without body unchanged`() {
        assertEquals("/moskva/search", buildSearchUrl(SearchUrlParts(citySlug = "moskva")))
        val parsed = parseSearchUrl("/moskva/search?page=2&seatsMin=4")
        assertEquals("moskva", parsed.citySlug)
        assertNull(parsed.bodyType)
        assertNull(parsed.brandSlug)
        assertEquals(2, parsed.page)
        assertEquals(4, parsed.seatsMin)
    }

    @Test
    fun `city search with body query still round trips`() {
        val original =
            SearchUrlParts(
                citySlug = "moskva",
                bodyType = "SUV",
                bodyTypeLabel = "Внедорожник",
                page = 2,
            )
        // Explicit city+body without going through body-path preference: use query-only URL parse
        val parsed = parseSearchUrl("/moskva/search?bodyType=SUV&bodyTypeLabel=%D0%92%D0%BD%D0%B5%D0%B4%D0%BE%D1%80%D0%BE%D0%B6%D0%BD%D0%B8%D0%BA&page=2")
        assertEquals("moskva", parsed.citySlug)
        assertEquals("SUV", parsed.bodyType)
        assertEquals("Внедорожник", parsed.bodyTypeLabel)
        assertEquals(2, parsed.page)
        assertNull(parsed.brandSlug)
        // Re-build from city+body prefers body path (canonical emit)
        assertEquals("/search/suv?page=2", buildSearchUrl(original))
    }

    @Test
    fun `search toyota remains brand not body`() {
        val parsed = parseSearchUrl("/search/toyota")
        assertEquals("toyota", parsed.brandSlug)
        assertNull(parsed.bodyType)
    }

    @Test
    fun `body path with page and dates`() {
        val url =
            buildSearchUrl(
                SearchUrlParts(
                    bodyType = "Minivan",
                    bodyTypeLabel = "Минивэн",
                    startDate = "2025-02-01",
                    page = 2,
                ),
            )
        assertEquals("/search/minivan?startDate=2025-02-01&page=2", url)
        val parsed = parseSearchUrl(url)
        assertEquals("Minivan", parsed.bodyType)
        assertEquals(2, parsed.page)
        assertNull(parsed.brandSlug)
    }
}
