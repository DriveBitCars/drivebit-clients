package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebBasePathTest {
    @Test
    fun githubPagesHostIsDetected() {
        assertTrue(isGithubPagesHost("drivebitcars.github.io"))
        assertTrue(isGithubPagesHost("DriveBitCars.GitHub.io"))
        assertFalse(isGithubPagesHost("drivebit.ru"))
        assertFalse(isGithubPagesHost("localhost"))
    }

    @Test
    fun stripWebBasePath_removesGithubPagesPrefix() {
        assertEquals("/", stripWebBasePath("/drivebit-clients", "drivebitcars.github.io"))
        assertEquals("/", stripWebBasePath("/drivebit-clients/", "drivebitcars.github.io"))
        assertEquals("/moskva", stripWebBasePath("/drivebit-clients/moskva", "drivebitcars.github.io"))
        assertEquals(
            "/moskva/search/",
            stripWebBasePath("/drivebit-clients/moskva/search/", "drivebitcars.github.io"),
        )
    }

    @Test
    fun stripWebBasePath_keepsPathOnProduction() {
        assertEquals("/moskva", stripWebBasePath("/moskva", "drivebit.ru"))
        assertEquals(
            "/drivebit-clients/moskva",
            stripWebBasePath("/drivebit-clients/moskva", "drivebit.ru"),
        )
    }

    @Test
    fun withWebBasePath_prefixesGithubPages() {
        assertEquals(
            "/drivebit-clients/moskva",
            withWebBasePath("/moskva", "drivebitcars.github.io"),
        )
        assertEquals(
            "/drivebit-clients/login-by-phone?x=1",
            withWebBasePath("/login-by-phone?x=1", "drivebitcars.github.io"),
        )
        assertEquals(
            "/drivebit-clients/moskva",
            withWebBasePath("/drivebit-clients/moskva", "drivebitcars.github.io"),
        )
    }

    @Test
    fun withWebBasePath_keepsPathOnProduction() {
        assertEquals("/moskva", withWebBasePath("/moskva", "drivebit.ru"))
    }
}
