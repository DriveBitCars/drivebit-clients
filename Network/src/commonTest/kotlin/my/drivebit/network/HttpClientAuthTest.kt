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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

            val mockAuth = MockAuthForTest()
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

            val mockAuth = MockAuthForTest()
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
                MockAuthForTest().apply {
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
