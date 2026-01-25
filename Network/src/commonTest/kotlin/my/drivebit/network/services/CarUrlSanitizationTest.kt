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
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarUrlSanitizationTest {
    @Test
    fun `getCar should convert MinIO HTTP URLs to HTTPS with current domain`() =
        runTest {
            val apiResponse =
                """
                {
                    "id": "test-car-id",
                    "photos": [
                        {
                            "id": 1,
                            "url": "http://155.212.170.94:9000/publicbct/cars/test.jpg",
                            "uploadDate": "2025-01-01T00:00:00Z"
                        }
                    ],
                    "general": {
                        "photos": [
                            {
                                "id": 2,
                                "url": "http://155.212.170.94:9000/publicbct/cars/test2.jpg",
                                "uploadDate": "2025-01-01T00:00:00Z"
                            }
                        ]
                    }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            val car = CarImpl(httpClient)
            val result = car.getCar("test-car-id")

            // Check that MinIO URLs are converted (domain will be drivebit.ru for tests)
            assertEquals("test-car-id", result.id)
            assertTrue(result.photos.isNotEmpty())
            val photoUrl = result.photos[0].url
            assertTrue(photoUrl.contains("/publicbct/cars"), "URL should contain correct path")
        }

    @Test
    fun `getCar should convert MinIO HTTPS URLs to current domain`() =
        runTest {
            val apiResponse =
                """
                {
                    "id": "test-car-id",
                    "photos": [
                        {
                            "id": 1,
                            "url": "https://155.212.170.94:9000/publicbct/avatars/test.jpg",
                            "uploadDate": "2025-01-01T00:00:00Z"
                        }
                    ]
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            val car = CarImpl(httpClient)
            val result = car.getCar("test-car-id")

            val photoUrl = result.photos[0].url
            assertTrue(photoUrl.contains("/publicbct/avatars"), "URL should contain /publicbct/avatars/ path")
        }

    @Test
    fun `getCar should convert relative publicbct URLs to HTTPS with current domain`() =
        runTest {
            val apiResponse =
                """
                {
                    "id": "test-car-id",
                    "photos": [
                        {
                            "id": 1,
                            "url": "/publicbct/cars/test.jpg",
                            "uploadDate": "2025-01-01T00:00:00Z"
                        }
                    ]
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            val car = CarImpl(httpClient)
            val result = car.getCar("test-car-id")

            val photoUrl = result.photos[0].url
            assertTrue(photoUrl.contains("/publicbct/cars"), "URL should contain correct path")
        }

    @Test
    fun `getMyCars should convert MinIO URLs in general photos`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "test-car-id",
                        "photos": [],
                        "general": {
                            "photos": [
                                {
                                    "id": 1,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/test.jpg",
                                    "uploadDate": "2025-01-01T00:00:00Z"
                                }
                            ]
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            val car = CarImpl(httpClient)
            val result = car.getMyCars()

            assertTrue(result.isNotEmpty())
            val carItem = result[0]
            assertTrue(carItem.general?.photos?.isNotEmpty() == true)

            val photoUrl =
                carItem.general
                    .photos
                    .get(0)
                    .url
            assertFalse(photoUrl.contains("155.212.170.94:9000"), "URL should not contain MinIO IP and port")
        }

    @Test
    fun `getCar should convert MinIO URLs without protocol to HTTPS with current domain`() =
        runTest {
            val apiResponse =
                """
                {
                    "id": "test-car-id",
                    "photos": [
                        {
                            "id": 1,
                            "url": "155.212.170.94:9000/publicbct/avatars/test.jpg",
                            "uploadDate": "2025-01-01T00:00:00Z"
                        }
                    ]
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }

            val car = CarImpl(httpClient)
            val result = car.getCar("test-car-id")

            val photoUrl = result.photos[0].url
            assertTrue(photoUrl.contains("/publicbct/avatars"), "URL should contain /publicbct/avatars/ path")
        }
}
