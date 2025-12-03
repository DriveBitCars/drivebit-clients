package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.AccessTokenDTO
import my.drivebit.network.services.CreateNewTokensResponse
import my.drivebit.network.services.RefreshTokenDTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import my.drivebit.network.services.Auth as AuthService

class MockAuthForHttpClientTest : AuthService {
    var shouldThrowError = false
    var errorMessage = "Refresh failed"
    var refreshCallCount = 0

    override suspend fun createOtp(login: String) = throw NotImplementedError()

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ) = throw NotImplementedError()

    override suspend fun createTokens(refreshToken: String): CreateNewTokensResponse {
        refreshCallCount++
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return CreateNewTokensResponse(
            accessToken =
                AccessTokenDTO(
                    token = "new-access-token-$refreshCallCount",
                    expiresAt = "2025-12-02T16:00:00Z",
                ),
            refreshToken =
                RefreshTokenDTO(
                    token = "new-refresh-token-$refreshCallCount",
                    userId = "user-123",
                    expiresAt = "2025-12-09T16:00:00Z",
                    createdAt = "2025-12-02T15:00:00Z",
                ),
        )
    }
}

class HttpClientAuthTest {
    @Test
    fun `HttpClient should add Bearer token to Authorization header`() =
        runTest {
            var capturedAuthHeader: String? = null

            val mockEngine =
                MockEngine { request ->
                    capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                    respond(
                        content = """{"result": "success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val mockAuth = MockAuthForHttpClientTest()
            var savedAccessToken: String? = null
            var savedRefreshToken: String? = null

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                    install(Auth) {
                        bearer {
                            loadTokens {
                                BearerTokens(
                                    accessToken = "test-access-token-123",
                                    refreshToken = "test-refresh-token-456",
                                )
                            }
                        }
                    }
                }

            httpClient.get("https://example.com/api/test")

            assertNotNull(capturedAuthHeader)
            assertEquals("Bearer test-access-token-123", capturedAuthHeader)
        }

    @Test
    fun `HttpClient should automatically refresh token on 401 and retry request`() =
        runTest {
            var requestCount = 0
            var capturedAuthHeader: String? = null

            val mockEngine =
                MockEngine { request ->
                    requestCount++
                    capturedAuthHeader = request.headers[HttpHeaders.Authorization]

                    when (requestCount) {
                        1 -> {
                            respond(
                                content = """"Unauthorized"""",
                                status = HttpStatusCode.Unauthorized,
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                        2 -> {
                            respond(
                                content = """{"result": "success"}""",
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                        else -> {
                            respond(
                                content = """{"result": "success"}""",
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                    }
                }

            val mockAuth = MockAuthForHttpClientTest()
            var savedAccessToken: String? = null
            var savedRefreshToken: String? = null

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                    install(Auth) {
                        bearer {
                            loadTokens {
                                BearerTokens(
                                    accessToken = savedAccessToken ?: "old-access-token",
                                    refreshToken = savedRefreshToken ?: "old-refresh-token",
                                )
                            }
                            refreshTokens {
                                val refreshTokenValue = oldTokens?.refreshToken
                                if (refreshTokenValue != null) {
                                    try {
                                        val newTokens = mockAuth.createTokens(refreshTokenValue)
                                        val newAccessToken = newTokens.accessToken.token
                                        val newRefreshToken = newTokens.refreshToken.token

                                        savedAccessToken = newAccessToken
                                        savedRefreshToken = newRefreshToken
                                        BearerTokens(
                                            accessToken = newAccessToken,
                                            refreshToken = newRefreshToken,
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }

            httpClient.get("https://example.com/api/test")

            assertEquals(2, requestCount)
            assertEquals(1, mockAuth.refreshCallCount)
            assertEquals("new-access-token-1", savedAccessToken)
            assertEquals("new-refresh-token-1", savedRefreshToken)
            assertNotNull(capturedAuthHeader)
            assertEquals("Bearer new-access-token-1", capturedAuthHeader)
        }

    @Test
    fun `HttpClient should not refresh token if refresh fails`() =
        runTest {
            var requestCount = 0

            val mockEngine =
                MockEngine { request ->
                    requestCount++
                    respond(
                        content = """"Unauthorized"""",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val mockAuth =
                MockAuthForHttpClientTest().apply {
                    shouldThrowError = true
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                    install(Auth) {
                        bearer {
                            loadTokens {
                                BearerTokens(
                                    accessToken = "old-access-token",
                                    refreshToken = "invalid-refresh-token",
                                )
                            }
                            refreshTokens {
                                val refreshTokenValue = oldTokens?.refreshToken
                                if (refreshTokenValue != null) {
                                    try {
                                        val newTokens = mockAuth.createTokens(refreshTokenValue)
                                        val newAccessToken = newTokens.accessToken.token
                                        val newRefreshToken = newTokens.refreshToken.token
                                        BearerTokens(
                                            accessToken = newAccessToken,
                                            refreshToken = newRefreshToken,
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }

            try {
                httpClient.get("https://example.com/api/test")
            } catch (e: Exception) {
            }

            assertEquals(1, requestCount)
            assertEquals(1, mockAuth.refreshCallCount)
        }
}
