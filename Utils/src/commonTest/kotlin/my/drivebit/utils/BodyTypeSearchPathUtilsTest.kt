package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BodyTypeSearchPathUtilsTest {
    @Test
    fun `maps sedan hatchback crossover suv minivan slugs`() {
        assertEquals("Sedan", bodyTypePathInfoBySlug("sedan")?.apiName)
        assertEquals("Седан", bodyTypePathInfoBySlug("sedan")?.label)
        assertEquals("Hatchback", bodyTypePathInfoBySlug("hatchback")?.apiName)
        assertEquals("Crossover", bodyTypePathInfoBySlug("crossover")?.apiName)
        assertEquals("SUV", bodyTypePathInfoBySlug("suv")?.apiName)
        assertEquals("Внедорожник", bodyTypePathInfoBySlug("suv")?.label)
        assertEquals("Minivan", bodyTypePathInfoBySlug("minivan")?.apiName)
    }

    @Test
    fun `resolves by api name case-insensitively`() {
        assertEquals("sedan", bodyTypePathInfoByApiName("Sedan")?.slug)
        assertEquals("suv", bodyTypePathInfoByApiName("suv")?.slug)
        assertEquals("suv", bodyTypePathInfoByApiName("SUV")?.slug)
    }

    @Test
    fun `pathForBodyTypeApiName returns search path`() {
        assertEquals("/search/sedan", pathForBodyTypeApiName("Sedan"))
        assertEquals("/search/suv", pathForBodyTypeApiName("SUV"))
        assertNull(pathForBodyTypeApiName("NotAType"))
    }

    @Test
    fun `isBodyTypeSearchSlug distinguishes body from brand samples`() {
        assertTrue(isBodyTypeSearchSlug("sedan"))
        assertTrue(isBodyTypeSearchSlug("SUV"))
        assertFalse(isBodyTypeSearchSlug("bmw"))
        assertFalse(isBodyTypeSearchSlug("audi"))
        assertFalse(isBodyTypeSearchSlug("toyota"))
        assertFalse(isBodyTypeSearchSlug("moskva"))
    }

    @Test
    fun `unknown slug returns null`() {
        assertNull(bodyTypePathInfoBySlug("not-a-body"))
        assertNull(bodyTypePathInfoBySlug(""))
    }

    @Test
    fun `all known body slugs are non-empty and unique`() {
        val slugs = BODY_TYPE_SEARCH_PATH_ENTRIES.map { it.slug }
        assertTrue(slugs.isNotEmpty())
        assertEquals(slugs.size, slugs.toSet().size)
        slugs.forEach { assertNotNull(bodyTypePathInfoBySlug(it)) }
    }
}
