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
        assertEquals("bmw", parseBrandSlugFromPath("/bmw/x5"))
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
    fun canonicalizeBrandSearchPath_preservesModelSlug() {
        assertEquals("/search/bmw/x5", canonicalizeBrandSearchPath("/search/bmw/x5"))
        assertEquals("/search/bmw/x5", canonicalizeBrandSearchPath("/bmw/x5"))
        assertEquals("/search/audi/a6", canonicalizeBrandSearchPath("/audi/a6"))
    }

    @Test
    fun parseBrandSlugFromPath_rejectsBodyTypeSlugs() {
        assertNull(parseBrandSlugFromPath("/search/sedan"))
        assertNull(parseBrandSlugFromPath("/search/suv"))
        assertNull(parseBrandSlugFromPath("/search/minivan/"))
        assertEquals("bmw", parseBrandSlugFromPath("/search/bmw/x5"))
    }

    @Test
    fun canonicalizeBrandSearchPath_rejectsBodyTypeSlugs() {
        assertNull(canonicalizeBrandSearchPath("/search/sedan"))
        assertNull(canonicalizeBrandSearchPath("/search/hatchback"))
        assertEquals("/search/bmw/x5", canonicalizeBrandSearchPath("/search/bmw/x5"))
    }

    @Test
    fun isShortBrandSearchPath_onlyLegacyAudiBmwBarePaths() {
        assertTrue(isShortBrandSearchPath("/audi"))
        assertTrue(isShortBrandSearchPath("/bmw/"))
        assertFalse(isShortBrandSearchPath("/search/audi"))
        assertFalse(isShortBrandSearchPath("/skoda"))
    }
}
