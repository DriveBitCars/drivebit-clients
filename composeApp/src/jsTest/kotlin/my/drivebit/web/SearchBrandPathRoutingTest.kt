package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchBrandPathRoutingTest {
    @Test
    fun parseSearchBrandSlug_returnsSlugForBrandPath() {
        assertEquals("bmw", parseSearchBrandSlugFromPath("/search/bmw"))
        assertEquals("skoda", parseSearchBrandSlugFromPath("/search/skoda/"))
    }

    @Test
    fun parseSearchBrandSlug_returnsNullForSearchRoot() {
        assertNull(parseSearchBrandSlugFromPath("/search"))
        assertNull(parseSearchBrandSlugFromPath("/search/"))
    }

    @Test
    fun parseSearchBrandSlug_returnsNullForAiSearch() {
        assertNull(parseSearchBrandSlugFromPath("/search/ai"))
    }

    @Test
    fun searchPathForBrandName_buildsSlugPath() {
        assertEquals("/search/bmw", searchPathForBrandName("BMW"))
        assertEquals("/search/mercedes-benz", searchPathForBrandName("Mercedes-Benz"))
    }
}
