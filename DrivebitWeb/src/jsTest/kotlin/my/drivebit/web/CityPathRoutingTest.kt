package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CityPathRoutingTest {
    @Test
    fun parseCityPath_singleSegmentCity() {
        val result = parseCityPath("/moskva")

        assertEquals(CityPathParts(citySlug = "moskva", filterSlug = null), result)
    }

    @Test
    fun parseCityPath_cityAndFilterSegment() {
        val result = parseCityPath("/moskva/poblizosti")

        assertEquals(CityPathParts(citySlug = "moskva", filterSlug = "poblizosti"), result)
    }

    @Test
    fun parseCityPath_ignoresTrailingSlashAndCase() {
        val result = parseCityPath("/Moskva/PobliZosti/")

        assertEquals(CityPathParts(citySlug = "moskva", filterSlug = "poblizosti"), result)
    }

    @Test
    fun parseCityPath_returnsNullForReservedFirstSegment() {
        val result = parseCityPath("/profile")

        assertNull(result)
    }

    @Test
    fun parseCityPath_returnsNullForDownloadBookingContract() {
        assertNull(parseCityPath("/download-booking-contract"))
        assertNull(parseCityPath("/download-booking-contract?bookingId=abc"))
    }

    @Test
    fun parseCityPath_stripsQueryAndReturnsNullForSearchWithParams() {
        assertNull(parseCityPath("/search?startDate=2026-04-25&endDate=2026-04-30"))
    }

    @Test
    fun parseCityPath_stripsQueryForCityRoot() {
        assertEquals(
            CityPathParts(citySlug = "moskva", filterSlug = null),
            parseCityPath("/moskva?ref=home"),
        )
    }

    @Test
    fun parseCityPath_returnsNullWhenTooManySegments() {
        val result = parseCityPath("/moskva/poblizosti/extra")

        assertNull(result)
    }

    @Test
    fun parseFilterSlugFromCityPath_returnsFilterSlug() {
        assertEquals("poblizosti", parseFilterSlugFromCityPath("/moskva/poblizosti"))
    }

    @Test
    fun parseFilterSlugFromCityPath_returnsNullForCityRoot() {
        assertNull(parseFilterSlugFromCityPath("/moskva"))
    }

    @Test
    fun cityPathWithFilter_allFilterUsesCityRootPath() {
        assertEquals("/moskva", cityPathWithFilter("moskva", "Все"))
    }

    @Test
    fun cityPathWithFilter_nonAllFilterAppendsSlug() {
        assertEquals("/moskva/poblizosti", cityPathWithFilter("moskva", "Поблизости"))
    }

    @Test
    fun filterTitleFromPathSegment_mapsKnownFilters() {
        assertEquals("Все", filterTitleFromPathSegment(null))
        assertEquals("Поблизости", filterTitleFromPathSegment("poblizosti"))
        assertEquals("Путешествия", filterTitleFromPathSegment("puteshestviya"))
        assertEquals("За город", filterTitleFromPathSegment("za-gorod"))
    }

    @Test
    fun cityPathWithFilter_vnedorozhnikUsesSeoSlug() {
        assertEquals(
            "/moskva/arenda-vnedorozhnika-bez-voditelya",
            cityPathWithFilter("moskva", "Внедорожник"),
        )
    }

    @Test
    fun filterTitleFromPathSegment_unknownSlugDefaultsToAll() {
        assertEquals("Все", filterTitleFromPathSegment("unknown-filter"))
    }

    @Test
    fun filterTitleToPathSegment_vnedorozhnikUsesSeoSlug() {
        assertEquals("arenda-vnedorozhnika-bez-voditelya", filterTitleToPathSegment("Внедорожник"))
        assertEquals("Внедорожник", filterTitleFromPathSegment("arenda-vnedorozhnika-bez-voditelya"))
    }

    @Test
    fun cityPathWithFilter_minivenUsesSeoSlug() {
        assertEquals(
            "/moskva/arenda-minivena-bez-voditelya",
            cityPathWithFilter("moskva", "Минивэн"),
        )
    }

    @Test
    fun filterTitleToPathSegment_minivenUsesSeoSlug() {
        assertEquals("arenda-minivena-bez-voditelya", filterTitleToPathSegment("Минивэн"))
        assertEquals("Минивэн", filterTitleFromPathSegment("arenda-minivena-bez-voditelya"))
    }

    @Test
    fun parseCitySlugFromPath_supportedSitemapCityUrls() {
        val urls =
            listOf(
                "/moskva",
                "/moskva/k-rodnym",
                "/moskva/puteshestviya",
                "/moskva/komandirovki",
                "/moskva/za-gorod",
                "/moskva/kanikuly",
                "/moskva/arenda-minivena-bez-voditelya",
                "/moskva/arenda-vnedorozhnika-bez-voditelya",
                "/zelenograd",
                "/kaliningrad",
                "/krasnogorsk",
                "/lyubertsy",
            )

        urls.forEach { path ->
            assertNotNull(parseCitySlugFromPath(path), "Expected city slug to be parsed for $path")
        }
    }
}
