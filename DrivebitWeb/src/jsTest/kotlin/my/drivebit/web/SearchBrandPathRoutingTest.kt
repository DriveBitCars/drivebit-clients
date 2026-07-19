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
    fun parseSearchBrandSlug_returnsSlugForLegacyShortAudiBmwPaths() {
        assertEquals("audi", parseSearchBrandSlugFromPath("/audi"))
        assertEquals("bmw", parseSearchBrandSlugFromPath("/bmw/"))
    }

    @Test
    fun parseSearchBrandSlug_returnsNullForSearchRoot() {
        assertNull(parseSearchBrandSlugFromPath("/search"))
        assertNull(parseSearchBrandSlugFromPath("/search/"))
    }

    @Test
    fun searchPathForBrandName_usesSearchPrefixForAllBrands() {
        assertEquals("/search/audi", searchPathForBrandName("Audi"))
        assertEquals("/search/bmw", searchPathForBrandName("BMW"))
        assertEquals("/search/mercedes-benz", searchPathForBrandName("Mercedes-Benz"))
        assertEquals("/search/skoda", searchPathForBrandName("Skoda"))
    }

    @Test
    fun isSearchBrandPath_matchesLegacyShortAndPrefixed() {
        assertTrue(isSearchBrandPath("/audi"))
        assertTrue(isSearchBrandPath("/bmw"))
        assertTrue(isSearchBrandPath("/search/bmw"))
    }

    @Test
    fun canonicalSearchBrandPath_mapsLegacyShortToSearchPrefix() {
        assertEquals("/search/audi", canonicalSearchBrandPath("/audi"))
        assertEquals("/search/bmw", canonicalSearchBrandPath("/bmw"))
        assertEquals("/search/bmw", canonicalSearchBrandPath("/search/bmw"))
    }
}
