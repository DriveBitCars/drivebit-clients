package my.drivebit.web.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HawkErrorTrackingTest {
    @Test
    fun productionHostUsesProductionEnvironment() {
        assertEquals("production", hawkEnvironment("drivebit.ru"))
    }

    @Test
    fun pagesDevHostUsesDevelopmentEnvironment() {
        assertEquals("development", hawkEnvironment("dev.drivebit.my"))
    }

    @Test
    fun localhostDoesNotInitializeHawk() {
        assertNull(hawkEnvironment("localhost"))
    }

    @Test
    fun opaqueScriptErrorIsNotSentToHawk() {
        assertFalse(shouldSendHawkEvent("Script error."))
        assertFalse(shouldSendHawkEvent("Script error"))
        assertFalse(shouldSendHawkEvent("  script error.  "))
    }

    @Test
    fun realErrorsAreStillSentToHawk() {
        assertTrue(shouldSendHawkEvent("TypeError: Cannot read properties of null"))
        assertTrue(shouldSendHawkEvent(null))
        assertTrue(shouldSendHawkEvent(""))
    }
}
