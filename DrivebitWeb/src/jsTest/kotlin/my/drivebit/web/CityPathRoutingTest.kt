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
        assertEquals("В Крым", filterTitleFromPathSegment("arenda-avto-v-krym"))
        assertEquals("Беларусь", filterTitleFromPathSegment("arenda-avto-v-belarus"))
        assertEquals("Абхазия", filterTitleFromPathSegment("arenda-avto-v-abkhaziyu"))
    }

    @Test
    fun cityPathWithFilter_destinationFiltersUseSeoSlug() {
        assertEquals("/moskva/arenda-avto-v-krym", cityPathWithFilter("moskva", "В Крым"))
        assertEquals("/moskva/arenda-avto-v-belarus", cityPathWithFilter("moskva", "Беларусь"))
        assertEquals("/moskva/arenda-avto-v-abkhaziyu", cityPathWithFilter("moskva", "Абхазия"))
    }

    @Test
    fun filterTitleToPathSegment_destinationFiltersUseSeoSlug() {
        assertEquals("arenda-avto-v-krym", filterTitleToPathSegment("В Крым"))
        assertEquals("arenda-avto-v-belarus", filterTitleToPathSegment("Беларусь"))
        assertEquals("arenda-avto-v-abkhaziyu", filterTitleToPathSegment("Абхазия"))
    }

    @Test
    fun cityPathWithFilter_ekonomUsesSeoSlug() {
        assertEquals(
            "/moskva/arenda-avto-ekonom-klassa-bez-voditelya",
            cityPathWithFilter("moskva", "Эконом"),
        )
    }

    @Test
    fun cityPathWithFilter_premiumUsesSeoSlug() {
        assertEquals(
            "/moskva/arenda-avto-premium-klassa-bez-voditelya",
            cityPathWithFilter("moskva", "Премиум"),
        )
    }

    @Test
    fun filterTitleToPathSegment_ekonomAndPremiumUseSeoSlug() {
        assertEquals("arenda-avto-ekonom-klassa-bez-voditelya", filterTitleToPathSegment("Эконом"))
        assertEquals("Эконом", filterTitleFromPathSegment("arenda-avto-ekonom-klassa-bez-voditelya"))
        assertEquals("arenda-avto-premium-klassa-bez-voditelya", filterTitleToPathSegment("Премиум"))
        assertEquals("Премиум", filterTitleFromPathSegment("arenda-avto-premium-klassa-bez-voditelya"))
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
                "/moskva/arenda-avto-v-krym",
                "/moskva/arenda-avto-v-belarus",
                "/moskva/arenda-avto-v-abkhaziyu",
                "/moskva/arenda-avto-ekonom-klassa-bez-voditelya",
                "/moskva/arenda-avto-premium-klassa-bez-voditelya",
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
