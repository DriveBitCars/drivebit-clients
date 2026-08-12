package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarPhotoSwipeTest {
    @Test
    fun `swipe left advances to next photo when not on last`() {
        assertEquals(
            1,
            resolveCarPhotoSwipeIndex(
                currentIndex = 0,
                photoCount = 3,
                startX = 200.0,
                endX = 140.0,
            ),
        )
    }

    @Test
    fun `swipe right goes to previous photo when not on first`() {
        assertEquals(
            0,
            resolveCarPhotoSwipeIndex(
                currentIndex = 1,
                photoCount = 3,
                startX = 140.0,
                endX = 200.0,
            ),
        )
    }

    @Test
    fun `small movement does not change photo`() {
        assertEquals(
            1,
            resolveCarPhotoSwipeIndex(
                currentIndex = 1,
                photoCount = 3,
                startX = 160.0,
                endX = 140.0,
            ),
        )
    }

    @Test
    fun `swipe left on last photo stays on last`() {
        assertEquals(
            2,
            resolveCarPhotoSwipeIndex(
                currentIndex = 2,
                photoCount = 3,
                startX = 200.0,
                endX = 100.0,
            ),
        )
    }

    @Test
    fun `swipe right on first photo stays on first`() {
        assertEquals(
            0,
            resolveCarPhotoSwipeIndex(
                currentIndex = 0,
                photoCount = 3,
                startX = 100.0,
                endX = 200.0,
            ),
        )
    }

    @Test
    fun `single photo never changes index`() {
        assertEquals(
            0,
            resolveCarPhotoSwipeIndex(
                currentIndex = 0,
                photoCount = 1,
                startX = 200.0,
                endX = 50.0,
            ),
        )
    }

    @Test
    fun `swipe movement is significant when abs delta exceeds threshold`() {
        assertTrue(isCarPhotoSwipeSignificant(startX = 200.0, endX = 150.0))
        assertFalse(isCarPhotoSwipeSignificant(startX = 200.0, endX = 180.0))
    }
}
