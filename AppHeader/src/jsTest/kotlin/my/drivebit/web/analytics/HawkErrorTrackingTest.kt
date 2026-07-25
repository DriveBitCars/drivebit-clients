package my.drivebit.web.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
}
