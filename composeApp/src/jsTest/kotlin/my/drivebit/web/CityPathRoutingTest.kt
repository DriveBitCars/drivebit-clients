package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
