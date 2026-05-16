package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinioUrlUtilsTest {
    @Test
    fun extractPath_stripsDirectMinioHost() {
        val u = "https://api.drivebit.ru:9000/publicbct/avatars/a.jpg"
        assertEquals("/publicbct/avatars/a.jpg", extractPathFromApiUrl(u))
        assertTrue(isDirectMinioUrl(u))
    }

    @Test
    fun extractPath_stripsHttpMinioHost() {
        val u = "http://api.drivebit.ru:9000/publicbct/cars/x.jpg"
        assertEquals("/publicbct/cars/x.jpg", extractPathFromApiUrl(u))
        assertTrue(isDirectMinioUrl(u))
    }

    @Test
    fun extractPath_stripsAnyMinioHostOn9000() {
        val u = "http://157.22.252.70:9000/publicbct/avatars/new.jpg"
        assertEquals("/publicbct/avatars/new.jpg", extractPathFromApiUrl(u))
        assertTrue(isDirectMinioUrl(u))
    }

    @Test
    fun extractPath_stripsMinioHostWithoutProtocol() {
        val u = "157.22.252.70:9000/publicbct/avatars/no-protocol.jpg"
        assertEquals("/publicbct/avatars/no-protocol.jpg", extractPathFromApiUrl(u))
        assertTrue(isDirectMinioUrl(u))
    }

    @Test
    fun extractPath_leavesOtherUrls() {
        val u = "https://drivebit.ru/publicbct/cars/x.jpg"
        assertEquals(u, extractPathFromApiUrl(u))
        assertFalse(isDirectMinioUrl(u))
    }
}
