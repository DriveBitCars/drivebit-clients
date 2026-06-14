package my.drivebit.network

import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenRefreshCoordinatorTest {
    @Test
    fun resolveRefreshTokenPrefersStorageOverStaleMemoryToken() {
        val resolved =
            resolveRefreshToken(
                getRefreshToken = { "storage-refresh" },
                staleRefreshToken = "memory-refresh",
            )

        assertEquals("storage-refresh", resolved)
    }

    @Test
    fun tokensFromStorageIfUpdatedReturnsFreshTokensFromAnotherTab() {
        val tokens =
            tokensFromStorageIfUpdated(
                getAccessToken = { "fresh-access" },
                getRefreshToken = { "fresh-refresh" },
                staleRefreshToken = "stale-refresh",
            )

        assertNotNull(tokens)
        assertEquals("fresh-access", tokens.accessToken)
        assertEquals("fresh-refresh", tokens.refreshToken)
    }

    @Test
    fun tokensFromStorageIfUpdatedReturnsNullWhenRefreshUnchanged() {
        val tokens =
            tokensFromStorageIfUpdated(
                getAccessToken = { "access" },
                getRefreshToken = { "same-refresh" },
                staleRefreshToken = "same-refresh",
            )

        assertNull(tokens)
    }

    @Test
    fun coordinatorUsesStorageTokensWhenAnotherTabAlreadyRefreshed() =
        runTest {
            var refreshCalls = 0
            var loggedOut = false
            val coordinator = TokenRefreshCoordinator()

            val recovered =
                coordinator.refreshTokens(
                    staleRefreshToken = "stale-refresh",
                    getAccessToken = { "tab2-access" },
                    getRefreshToken = { "tab2-refresh" },
                    saveTokens = { _, _ -> },
                    requestRefresh = {
                        refreshCalls++
                        "new-access" to "new-refresh"
                    },
                    onRefreshFailed = { loggedOut = true },
                )

            assertNotNull(recovered)
            assertEquals("tab2-access", recovered.accessToken)
            assertEquals("tab2-refresh", recovered.refreshToken)
            assertEquals(0, refreshCalls)
            assertFalse(loggedOut)
        }

    @Test
    fun coordinatorSerializesParallelRefreshRequests() =
        runTest {
            var refreshCalls = 0
            var accessToken = "expired-access"
            var refreshToken = "shared-refresh"
            val staleRefreshToken = "shared-refresh"
            val coordinator = TokenRefreshCoordinator()

            val refreshBlock: suspend () -> Unit = {
                coordinator.refreshTokens(
                    staleRefreshToken = staleRefreshToken,
                    getAccessToken = { accessToken },
                    getRefreshToken = { refreshToken },
                    saveTokens = { access, refresh ->
                        access?.let { accessToken = it }
                        refreshToken = refresh
                    },
                    requestRefresh = {
                        refreshCalls++
                        "new-access-$refreshCalls" to "new-refresh-$refreshCalls"
                    },
                    onRefreshFailed = {},
                )
            }

            val first = async { refreshBlock() }
            val second = async { refreshBlock() }
            first.await()
            second.await()

            assertEquals(1, refreshCalls)
            assertEquals("new-access-1", accessToken)
            assertEquals("new-refresh-1", refreshToken)
        }

    @Test
    fun coordinatorLogsOutWhenRefreshFailsAndStorageWasNotUpdated() =
        runTest {
            var loggedOut = false
            val coordinator = TokenRefreshCoordinator()

            val result =
                coordinator.refreshTokens(
                    staleRefreshToken = "stale-refresh",
                    getAccessToken = { "expired-access" },
                    getRefreshToken = { "stale-refresh" },
                    saveTokens = { _, _ -> },
                    requestRefresh = { throw IllegalStateException("invalid refresh token") },
                    onRefreshFailed = { loggedOut = true },
                )

            assertNull(result)
            assertTrue(loggedOut)
        }
}
