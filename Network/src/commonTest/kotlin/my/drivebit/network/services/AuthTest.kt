package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AuthTest {
    @Test
    fun `verifyOtp should throw NetworkException with error message on 500 status`() =
        runTest {
            val errorMessage =
                "IDX12401: Expires: '12/02/2025 14:39:53' must be after NotBefore: '12/02/2025 14:39:53'."
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "\"$errorMessage\"",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    auth.verifyOtp("session-id", "123456")
                }

            assertEquals(HttpStatusCode.InternalServerError, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `verifyOtp should throw NetworkException with error message on 400 status`() =
        runTest {
            val errorMessage = "Invalid OTP code"
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "\"$errorMessage\"",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    auth.verifyOtp("session-id", "wrong")
                }

            assertEquals(HttpStatusCode.BadRequest, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `verifyOtp should return VerifyOtpResponse on success`() =
        runTest {
            val successResponse =
                """
                {
                    "accessToken": {
                        "token": "access-token-123",
                        "expiresAt": "2025-12-02T15:00:00Z"
                    },
                    "refreshToken": {
                        "token": "refresh-token-456",
                        "userId": "user-123",
                        "expiresAt": "2025-12-09T15:00:00Z",
                        "createdAt": "2025-12-02T14:00:00Z"
                    }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val result = auth.verifyOtp("session-id", "123456")

            assertNotNull(result)
            assertEquals("access-token-123", result.accessToken.token)
            assertEquals("refresh-token-456", result.refreshToken.token)
        }

    @Test
    fun `createOtp should throw NetworkException on error status`() =
        runTest {
            val errorMessage = "Invalid phone number"
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "\"$errorMessage\"",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    auth.createOtp("invalid")
                }

            assertEquals(HttpStatusCode.BadRequest, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `createOtp should return CreateOtpResponse on success`() =
        runTest {
            val successResponse =
                """
                {
                    "message": "OTP sent successfully",
                    "sessionId": "session-123",
                    "expiresIn": 300
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val result = auth.createOtp("+1234567890")

            assertNotNull(result)
            assertEquals("session-123", result.sessionId)
            assertEquals(300, result.expiresIn)
            assertEquals("OTP sent successfully", result.message)
        }

    @Test
    fun `createTokens should throw NetworkException with error message on 400 status`() =
        runTest {
            val errorMessage = "Invalid refresh token"
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "\"$errorMessage\"",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    auth.createTokens("invalid-refresh-token")
                }

            assertEquals(HttpStatusCode.BadRequest, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `createTokens should throw NetworkException with error message on 401 status`() =
        runTest {
            val errorMessage = "Refresh token expired"
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = "\"$errorMessage\"",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    auth.createTokens("expired-refresh-token")
                }

            assertEquals(HttpStatusCode.Unauthorized, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `createTokens should return CreateNewTokensResponse on success`() =
        runTest {
            val successResponse =
                """
                {
                    "accessToken": {
                        "token": "new-access-token-789",
                        "expiresAt": "2025-12-02T16:00:00Z"
                    },
                    "refreshToken": {
                        "token": "new-refresh-token-789",
                        "userId": "user-123",
                        "expiresAt": "2025-12-09T16:00:00Z",
                        "createdAt": "2025-12-02T15:00:00Z"
                    }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json()
                    }
                }

            val auth = AuthImpl(httpClient)

            val result = auth.createTokens("old-refresh-token-456")

            assertNotNull(result)
            assertEquals("new-access-token-789", result.accessToken.token)
            assertEquals("new-refresh-token-789", result.refreshToken.token)
            assertEquals("user-123", result.refreshToken.userId)
        }
}
