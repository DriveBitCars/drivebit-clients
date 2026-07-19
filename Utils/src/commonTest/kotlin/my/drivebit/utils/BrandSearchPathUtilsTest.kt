package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrandSearchPathUtilsTest {
    @Test
    fun pathForBrandSlug_usesSearchPrefixForAllBrandsIncludingAudiAndBmw() {
        assertEquals("/search/audi", pathForBrandSlug("audi"))
        assertEquals("/search/bmw", pathForBrandSlug("BMW"))
        assertEquals("/search/skoda", pathForBrandSlug("skoda"))
        assertEquals("/search/mercedes-benz", pathForBrandSlug("mercedes-benz"))
    }

    @Test
    fun parseBrandSlugFromPath_acceptsLegacyShortAudiBmw() {
        assertEquals("audi", parseBrandSlugFromPath("/audi"))
        assertEquals("bmw", parseBrandSlugFromPath("/bmw/"))
    }

    @Test
    fun parseBrandSlugFromPath_acceptsSearchPrefixedBrands() {
        assertEquals("audi", parseBrandSlugFromPath("/search/audi"))
        assertEquals("bmw", parseBrandSlugFromPath("/search/bmw"))
        assertEquals("skoda", parseBrandSlugFromPath("/search/skoda/"))
    }

    @Test
    fun parseBrandSlugFromPath_rejectsSearchRootAndCities() {
        assertNull(parseBrandSlugFromPath("/search"))
        assertNull(parseBrandSlugFromPath("/search/"))
        assertNull(parseBrandSlugFromPath("/moskva"))
        assertNull(parseBrandSlugFromPath("/"))
    }

    @Test
    fun canonicalizeBrandSearchPath_mapsLegacyShortAudiBmwToSearchPrefix() {
        assertEquals("/search/audi", canonicalizeBrandSearchPath("/search/audi"))
        assertEquals("/search/bmw", canonicalizeBrandSearchPath("/search/bmw/"))
        assertEquals("/search/audi", canonicalizeBrandSearchPath("/audi"))
        assertEquals("/search/bmw", canonicalizeBrandSearchPath("/bmw"))
        assertEquals("/search/skoda", canonicalizeBrandSearchPath("/search/skoda"))
        assertNull(canonicalizeBrandSearchPath("/moskva"))
    }

    @Test
    fun isShortBrandSearchPath_onlyLegacyAudiBmwBarePaths() {
        assertTrue(isShortBrandSearchPath("/audi"))
        assertTrue(isShortBrandSearchPath("/bmw/"))
        assertFalse(isShortBrandSearchPath("/search/audi"))
        assertFalse(isShortBrandSearchPath("/skoda"))
    }
}
