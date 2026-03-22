package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarPhotoImageUploadTest {
    @Test
    fun `isWebpSource true for mime`() {
        assertTrue(isWebpSource("image/webp", "x.png"))
        assertTrue(isWebpSource("image/WEBP", "x"))
    }

    @Test
    fun `isWebpSource true for extension when mime empty`() {
        assertTrue(isWebpSource("", "photo.webp"))
        assertTrue(isWebpSource("", "x.WEBP"))
    }

    @Test
    fun `isWebpSource false for jpeg`() {
        assertFalse(isWebpSource("image/jpeg", "x.jpg"))
    }

    @Test
    fun `isProcessableCarPhotoImage accepts image mime`() {
        assertTrue(isProcessableCarPhotoImage("image/png", ""))
    }

    @Test
    fun `isProcessableCarPhotoImage accepts known extension when mime empty`() {
        assertTrue(isProcessableCarPhotoImage("", "a.webp"))
        assertTrue(isProcessableCarPhotoImage("", "b.jpg"))
    }

    @Test
    fun `isProcessableCarPhotoImage rejects unknown extension and empty mime`() {
        assertFalse(isProcessableCarPhotoImage("", "file.xyz"))
    }

    @Test
    fun `processedCarPhotoFileName uses output extension`() {
        assertTrue(processedCarPhotoFileName("shot.png", "webp").endsWith(".webp"))
        assertTrue(processedCarPhotoFileName("shot.png", "jpg").endsWith(".jpg"))
    }

    @Test
    fun `processedCarPhotoFileName handles multiple dots`() {
        assertTrue(processedCarPhotoFileName("a.b.webp", "jpg") == "a.b.jpg")
    }
}
