package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarGridImageLoadingTest {
    @Test
    fun `mobile first two cards load eagerly`() {
        assertTrue(carGridImageLoadsEagerly(0, isMobileViewport = true))
        assertTrue(carGridImageLoadsEagerly(1, isMobileViewport = true))
        assertFalse(carGridImageLoadsEagerly(2, isMobileViewport = true))
    }

    @Test
    fun `desktop first three cards load eagerly`() {
        assertTrue(carGridImageLoadsEagerly(0, isMobileViewport = false))
        assertTrue(carGridImageLoadsEagerly(2, isMobileViewport = false))
        assertFalse(carGridImageLoadsEagerly(3, isMobileViewport = false))
    }

    @Test
    fun `loading attr maps to eager or lazy`() {
        assertEquals("eager", carGridImageLoadingAttr(0, isMobileViewport = true))
        assertEquals("lazy", carGridImageLoadingAttr(2, isMobileViewport = true))
        assertEquals("lazy", carGridImageLoadingAttr(5, isMobileViewport = false))
    }
}
