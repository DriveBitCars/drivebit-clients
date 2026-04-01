package my.drivebit.network

import my.drivebit.network.services.AccessTokenDTO
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthOperationResponse
import my.drivebit.network.services.CreateNewTokensResponse
import my.drivebit.network.services.RefreshTokenDTO

class MockAuthForTest : Auth {
    var shouldThrowError = false
    var errorMessage = "Refresh failed"
    var refreshCallCount = 0
    var accessTokenPrefix = "new-access-token"
    var refreshTokenPrefix = "new-refresh-token"

    override suspend fun createOtp(login: String) = throw NotImplementedError()

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ) = throw NotImplementedError()

    override suspend fun login(
        login: String,
        password: String,
    ) = throw NotImplementedError()

    override suspend fun changePasswordViaOtp(
        identifier: String,
        code: String,
        newPassword: String,
    ): AuthOperationResponse = throw NotImplementedError()

    override suspend fun createTokens(refreshToken: String): CreateNewTokensResponse {
        refreshCallCount++
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return CreateNewTokensResponse(
            accessToken =
                AccessTokenDTO(
                    token = "$accessTokenPrefix-$refreshCallCount",
                    expiresAt = "2025-12-02T16:00:00Z",
                ),
            refreshToken =
                RefreshTokenDTO(
                    token = "$refreshTokenPrefix-$refreshCallCount",
                    userId = "user-123",
                    expiresAt = "2025-12-09T16:00:00Z",
                    createdAt = "2025-12-02T15:00:00Z",
                ),
        )
    }
}
