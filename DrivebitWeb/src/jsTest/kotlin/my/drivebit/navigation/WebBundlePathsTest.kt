package my.drivebit.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebBundlePathsTest {
    @Test
    fun profileAndAccountAreDifferentBundles() {
        assertEquals(WebBundle.Profile, webBundleForPath("/profile"))
        assertEquals(WebBundle.Profile, webBundleForPath("/profile/"))
        assertEquals(WebBundle.Account, webBundleForPath("/change-email"))
        assertEquals(WebBundle.Account, webBundleForPath("/edit-name"))
        assertEquals(WebBundle.Account, webBundleForPath("/change-password"))
    }

    @Test
    fun authBundlePathsAreDetected() {
        assertEquals(WebBundle.Auth, webBundleForPath("/verify-otp"))
        assertEquals(WebBundle.Auth, webBundleForPath("/login-by-phone"))
    }

    @Test
    fun profileToChangeEmailRequiresFullPageNavigation() {
        assertTrue(requiresFullPageNavigation("/profile", "/change-email"))
        assertTrue(requiresFullPageNavigation("/profile/", "/edit-name?firstName=Ivan"))
        assertTrue(requiresFullPageNavigation("/profile", "/change-password?login=test"))
    }

    @Test
    fun accountToVerifyOtpRequiresFullPageNavigation() {
        assertTrue(requiresFullPageNavigation("/change-email", "/verify-otp?id=1"))
    }

    @Test
    fun authToProfileRequiresFullPageNavigation() {
        assertTrue(requiresFullPageNavigation("/verify-otp", "/profile"))
    }

    @Test
    fun sameBundleNavigationStaysInSpaMode() {
        assertFalse(requiresFullPageNavigation("/profile", "/profile"))
        assertFalse(requiresFullPageNavigation("/change-email", "/change-phone"))
        assertFalse(requiresFullPageNavigation("/edit-name", "/change-email"))
        assertTrue(requiresFullPageNavigation("/moskva", "/moskva/search"))
        assertTrue(requiresFullPageNavigation("/moskva", "/search"))
        assertFalse(requiresFullPageNavigation("/moskva/search", "/kaliningrad/search"))
    }

    @Test
    fun mainAppToProfileRequiresFullPageNavigation() {
        assertTrue(requiresFullPageNavigation("/moskva", "/profile"))
        assertTrue(requiresFullPageNavigation("/", "/profile"))
    }

    @Test
    fun splitBundleHrefNormalizesTrailingSlashAndPreservesQuery() {
        assertEquals("/edit-name/?firstName=Ivan", splitBundleHref("/edit-name", "?firstName=Ivan", ""))
        assertEquals("/change-email/", splitBundleHref("/change-email/", "", ""))
    }

    @Test
    fun isAnySplitBundlePathCoversProfileAndAccountRoutes() {
        assertTrue(isAnySplitBundlePath("/profile"))
        assertTrue(isAnySplitBundlePath("/edit-name"))
        assertTrue(isAnySplitBundlePath("/change-email"))
        assertTrue(isAnySplitBundlePath("/verify-otp"))
        assertFalse(isAnySplitBundlePath("/moskva"))
    }
}
