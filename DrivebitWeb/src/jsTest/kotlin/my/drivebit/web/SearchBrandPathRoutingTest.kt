package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchBrandPathRoutingTest {
    @Test
    fun parseSearchBrandSlug_returnsSlugForBrandPath() {
        assertEquals("bmw", parseSearchBrandSlugFromPath("/search/bmw"))
        assertEquals("skoda", parseSearchBrandSlugFromPath("/search/skoda/"))
    }

    @Test
    fun parseSearchBrandSlug_returnsSlugForShortAudiBmwPaths() {
        assertEquals("audi", parseSearchBrandSlugFromPath("/audi"))
        assertEquals("bmw", parseSearchBrandSlugFromPath("/bmw/"))
    }

    @Test
    fun parseSearchBrandSlug_returnsNullForSearchRoot() {
        assertNull(parseSearchBrandSlugFromPath("/search"))
        assertNull(parseSearchBrandSlugFromPath("/search/"))
    }

    @Test
    fun searchPathForBrandName_usesShortPathForAudiAndBmw() {
        assertEquals("/audi", searchPathForBrandName("Audi"))
        assertEquals("/bmw", searchPathForBrandName("BMW"))
    }

    @Test
    fun searchPathForBrandName_keepsSearchPrefixForOtherBrands() {
        assertEquals("/search/mercedes-benz", searchPathForBrandName("Mercedes-Benz"))
        assertEquals("/search/skoda", searchPathForBrandName("Skoda"))
    }

    @Test
    fun isSearchBrandPath_matchesShortAndPrefixed() {
        assertTrue(isSearchBrandPath("/audi"))
        assertTrue(isSearchBrandPath("/bmw"))
        assertTrue(isSearchBrandPath("/search/bmw"))
    }
}
