package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class CitySlugUtilsTest {
    @Test
    fun moscow() {
        assertEquals("moskva", cityNameToSlug("Москва"))
    }

    @Test
    fun saintPetersburg() {
        assertEquals("sankt-peterburg", cityNameToSlug("Санкт-Петербург"))
    }

    @Test
    fun kazan() {
        assertEquals("kazan", cityNameToSlug("Казань"))
    }

    @Test
    fun yoshkarOla() {
        assertEquals("yoshkar-ola", cityNameToSlug("Йошкар-Ола"))
    }

    @Test
    fun stableLowercaseAndTrim() {
        assertEquals("moskva", cityNameToSlug("  москва  "))
    }

    @Test
    fun emptyFallsBack() {
        assertEquals("city", cityNameToSlug("   "))
        assertEquals("city", cityNameToSlug("@@@"))
    }
}
