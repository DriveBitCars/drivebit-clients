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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun `userGet should throw NetworkException with error message on 500 status`() =
        runTest {
            val errorMessage = "Internal server error"
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

            val user = UserImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    user.userGet()
                }

            assertEquals(HttpStatusCode.InternalServerError, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `userGet should throw NetworkException with error message on 401 status`() =
        runTest {
            val errorMessage = "Unauthorized"
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

            val user = UserImpl(httpClient)

            val exception =
                assertFailsWith<NetworkException> {
                    user.userGet()
                }

            assertEquals(HttpStatusCode.Unauthorized, exception.statusCode)
            assertEquals(errorMessage, exception.message)
        }

    @Test
    fun `userGet should return UserGetResponse on success`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "phone": "+1234567890",
                    "firstName": "John",
                    "lastName": "Doe",
                    "middleName": "Middle",
                    "email": "user@example.com",
                    "createdAt": "2025-12-02T19:14:47.914Z",
                    "photos": ["photo1.jpg", "photo2.jpg"],
                    "isPassportVerified": true,
                    "isDriverLicenseVerified": true
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", result.id)
            assertEquals("+1234567890", result.phone)
            assertEquals("John", result.firstName)
            assertEquals("Doe", result.lastName)
            assertEquals("Middle", result.middleName)
            assertEquals("user@example.com", result.email)
            assertEquals("2025-12-02T19:14:47.914Z", result.createdAt)
            assertEquals(2, result.photos.size)
            assertEquals("photo1.jpg", result.photos[0])
            assertEquals("photo2.jpg", result.photos[1])
            assertTrue(result.isPassportVerified)
            assertTrue(result.isDriverLicenseVerified)
        }

    @Test
    fun `userGet defaults verification flags to false when absent`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "phone": "+1234567890",
                    "firstName": "John",
                    "lastName": "Doe",
                    "createdAt": "2025-12-02T19:14:47.914Z",
                    "photos": []
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
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

            val result = UserImpl(httpClient).userGet()

            assertFalse(result.isPassportVerified)
            assertFalse(result.isDriverLicenseVerified)
        }

    @Test
    fun `userGet should return UserGetResponse with partial data on success`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "phone": "+9876543210",
                    "firstName": "Jane",
                    "createdAt": ""
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", result.id)
            assertEquals("+9876543210", result.phone)
            assertEquals("Jane", result.firstName)
            assertEquals(emptyList<String>(), result.photos)
        }

    @Test
    fun `userGet should parse createdAt with microseconds correctly`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "test-id",
                    "createdAt": "2025-12-02T14:26:55.121463Z"
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("2025-12-02T14:26:55.121463Z", result.createdAt)
        }

    @Test
    fun `userGet should parse createdAt without microseconds correctly`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "test-id",
                    "createdAt": "2025-12-02T14:26:55Z"
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("2025-12-02T14:26:55Z", result.createdAt)
        }

    @Test
    fun `userGet should parse createdAt with milliseconds correctly`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "test-id",
                    "createdAt": "2025-12-02T19:14:47.914Z"
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("2025-12-02T19:14:47.914Z", result.createdAt)
        }

    @Test
    fun `userGet should handle null createdAt`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "test-id",
                    "createdAt": ""
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

            val user = UserImpl(httpClient)

            val result = user.userGet()

            assertNotNull(result)
            assertEquals("", result.createdAt)
        }
}
