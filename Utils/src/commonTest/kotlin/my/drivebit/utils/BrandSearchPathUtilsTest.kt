package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrandSearchPathUtilsTest {
    @Test
    fun pathForBrandSlug_usesShortPathForAudiAndBmw() {
        assertEquals("/audi", pathForBrandSlug("audi"))
        assertEquals("/bmw", pathForBrandSlug("BMW"))
    }

    @Test
    fun pathForBrandSlug_keepsSearchPrefixForOtherBrands() {
        assertEquals("/search/skoda", pathForBrandSlug("skoda"))
        assertEquals("/search/mercedes-benz", pathForBrandSlug("mercedes-benz"))
    }

    @Test
    fun parseBrandSlugFromPath_acceptsShortAudiBmw() {
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
    fun canonicalizeBrandSearchPath_mapsLegacyAudiBmwToShort() {
        assertEquals("/audi", canonicalizeBrandSearchPath("/search/audi"))
        assertEquals("/bmw", canonicalizeBrandSearchPath("/search/bmw/"))
        assertEquals("/audi", canonicalizeBrandSearchPath("/audi"))
        assertEquals("/search/skoda", canonicalizeBrandSearchPath("/search/skoda"))
        assertNull(canonicalizeBrandSearchPath("/moskva"))
    }

    @Test
    fun isShortBrandSearchPath_onlyAudiBmw() {
        assertTrue(isShortBrandSearchPath("/audi"))
        assertTrue(isShortBrandSearchPath("/bmw/"))
        assertFalse(isShortBrandSearchPath("/search/audi"))
        assertFalse(isShortBrandSearchPath("/skoda"))
    }
}
