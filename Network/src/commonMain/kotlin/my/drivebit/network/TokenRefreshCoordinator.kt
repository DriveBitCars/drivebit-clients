package my.drivebit.network

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TokenRefreshCoordinator {
    private val mutex = Mutex()

    suspend fun refreshTokens(
        staleRefreshToken: String?,
        getAccessToken: () -> String,
        getRefreshToken: () -> String,
        saveTokens: (String?, String) -> Unit,
        requestRefresh: suspend (String) -> Pair<String, String>,
        onRefreshFailed: () -> Unit,
    ): BearerTokens? {
        tokensFromStorageIfUpdated(getAccessToken, getRefreshToken, staleRefreshToken)?.let { return it }

        return mutex.withLock {
            tokensFromStorageIfUpdated(getAccessToken, getRefreshToken, staleRefreshToken)?.let { return it }

            val refreshToken = resolveRefreshToken(getRefreshToken, staleRefreshToken)
            if (refreshToken.isBlank()) {
                onRefreshFailed()
                return null
            }

            runCatching {
                val (accessToken, newRefreshToken) = requestRefresh(refreshToken)
                saveTokens(accessToken, newRefreshToken)
                BearerTokens(accessToken = accessToken, refreshToken = newRefreshToken)
            }.getOrElse {
                tokensFromStorageIfUpdated(getAccessToken, getRefreshToken, staleRefreshToken)
                    ?: run {
                        onRefreshFailed()
                        null
                    }
            }
        }
    }
}

internal fun resolveRefreshToken(
    getRefreshToken: () -> String,
    staleRefreshToken: String?,
): String {
    val fromStorage = getRefreshToken().trim()
    if (fromStorage.isNotBlank()) {
        return fromStorage
    }
    return staleRefreshToken?.trim().orEmpty()
}

internal fun tokensFromStorageIfUpdated(
    getAccessToken: () -> String,
    getRefreshToken: () -> String,
    staleRefreshToken: String?,
): BearerTokens? {
    val stale = staleRefreshToken?.trim().orEmpty()
    if (stale.isBlank()) {
        return null
    }

    val accessToken = getAccessToken().trim()
    val refreshToken = getRefreshToken().trim()
    if (accessToken.isBlank() || refreshToken.isBlank() || refreshToken == stale) {
        return null
    }

    return BearerTokens(accessToken = accessToken, refreshToken = refreshToken)
}
