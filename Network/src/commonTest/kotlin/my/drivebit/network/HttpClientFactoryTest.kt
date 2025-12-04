package my.drivebit.network

import my.drivebit.network.services.Auth
import kotlin.test.Test
import kotlin.test.assertNotNull

class MockAuthForTest : Auth {
    override suspend fun createOtp(login: String) = throw NotImplementedError()

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ) = throw NotImplementedError()

    override suspend fun createTokens(refreshToken: String) = throw NotImplementedError()
}

class HttpClientFactoryTest {
    @Test
    fun `createHttpClientWithConfig should create HttpClient with default base URL`() {
        val mockAuth = MockAuthForTest()
        val httpClient =
            createHttpClientWithConfig(
                getToken = { "test-token" },
                getRefreshToken = { "test-refresh-token" },
                saveTokens = { _, _ -> },
                authService = mockAuth,
            )
        assertNotNull(httpClient, "HttpClient should not be null")
    }
}
