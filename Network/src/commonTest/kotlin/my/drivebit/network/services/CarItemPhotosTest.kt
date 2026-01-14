package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import my.drivebit.network.parseResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CarItemPhotosTest {
    @Test
    fun `getMyCars should deserialize CarItem with photos from general field`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                        "brand": null,
                        "model": null,
                        "year": null,
                        "price": null,
                        "cityId": null,
                        "photos": [],
                        "general": {
                            "brandName": "Adam",
                            "modelName": "Revo",
                            "year": 0,
                            "licensePlate": "YAAAK",
                            "vin": "",
                            "seats": 0,
                            "mileage": 0,
                            "description": null,
                            "owner": "bf0ad237-da64-4542-8779-a803d6c7d3cc",
                            "ownerName": "",
                            "cityId": 158833,
                            "photos": [
                                {
                                    "id": 11,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/28005139-1f22-497d-84d8-6cba79b978f0_c",
                                    "uploadDate": "2026-01-04T19:23:58.147478Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                },
                                {
                                    "id": 12,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_c",
                                    "uploadDate": "2026-01-04T19:24:26.05663Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                },
                                {
                                    "id": 14,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/4c39d7ba-f09f-4514-b80f-662024e56395_c",
                                    "uploadDate": "2026-01-04T20:07:05.060429Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                }
                            ],
                            "address": {
                                "postalCode": "344033",
                                "region": "Ростовская",
                                "regionArea": "",
                                "cityType": "",
                                "city": null,
                                "street": "Жлобинский",
                                "house": "25",
                                "geoLat": 47.1968624,
                                "geoLon": 39.6330034
                            }
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.my/api/Car/my")
            val result: List<CarItem> = response.parseResponse()

            assertNotNull(result)
            assertEquals(1, result.size)

            val car = result.first()
            assertEquals("a575c0b1-3736-475f-a4a8-5a87cfbdb18a", car.id)
            assertNotNull(car.general)
            assertEquals("Adam", car.general?.brandName)
            assertEquals("Revo", car.general?.modelName)
            assertEquals("YAAAK", car.general?.licensePlate)

            assertNotNull(car.general?.photos)
            assertEquals(3, car.general?.photos?.size ?: 0, "Photos should be deserialized in general.photos")
            assertTrue(car.general?.photos?.isNotEmpty() == true, "Photos list should not be empty")

            val photosFromGeneral = car.general?.photos ?: emptyList()
            assertEquals(11, photosFromGeneral[0].id)
            assertEquals(
                "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/28005139-1f22-497d-84d8-6cba79b978f0_c",
                photosFromGeneral[0].url,
            )
            assertEquals(12, photosFromGeneral[1].id)
            assertEquals(
                "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_c",
                photosFromGeneral[1].url,
            )
            assertEquals(14, photosFromGeneral[2].id)
            assertEquals(
                "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/4c39d7ba-f09f-4514-b80f-662024e56395_c",
                photosFromGeneral[2].url,
            )
        }

    @Test
    fun `getMyCars should handle CarItem with empty photos array`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                        "photos": [],
                        "general": {
                            "brandName": "Adam",
                            "modelName": "Revo",
                            "year": 0,
                            "licensePlate": "YAAAK"
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.my/api/Car/my")
            val result: List<CarItem> = response.parseResponse()

            assertNotNull(result)
            assertEquals(1, result.size)

            val car = result.first()
            assertEquals("a575c0b1-3736-475f-a4a8-5a87cfbdb18a", car.id)
            assertTrue(car.photos.isEmpty(), "Photos list should be empty when API returns empty array")
        }

    @Test
    fun `getMyCars should handle CarItem with missing photos field`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                        "general": {
                            "brandName": "Adam",
                            "modelName": "Revo"
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.my/api/Car/my")
            val result: List<CarItem> = response.parseResponse()

            assertNotNull(result)
            assertEquals(1, result.size)

            val car = result.first()
            assertEquals("a575c0b1-3736-475f-a4a8-5a87cfbdb18a", car.id)
            assertTrue(
                car.photos.isEmpty(),
                "Photos list should be empty when photos field is missing (defaults to emptyList)",
            )
        }

    @Test
    fun `getMyCars should merge photos from general and top level`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                        "photos": [
                            {
                                "id": 1,
                                "url": "http://155.212.170.94:9000/publicbct/cars/top1.jpg",
                                "uploadDate": "2026-01-04T19:23:58.147478Z",
                                "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                            }
                        ],
                        "general": {
                            "brandName": "Adam",
                            "modelName": "Revo",
                            "photos": [
                                {
                                    "id": 2,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/general1.jpg",
                                    "uploadDate": "2026-01-04T19:24:26.05663Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                },
                                {
                                    "id": 3,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/general2.jpg",
                                    "uploadDate": "2026-01-04T20:07:05.060429Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                }
                            ]
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.my/api/Car/my")
            val result: List<CarItem> = response.parseResponse()

            assertNotNull(result)
            assertEquals(1, result.size)

            val car = result.first()
            assertEquals("a575c0b1-3736-475f-a4a8-5a87cfbdb18a", car.id)

            val photosFromGeneral = car.general?.photos ?: emptyList()
            val photosFromTopLevel = car.photos
            assertEquals(1, photosFromTopLevel.size, "Should have 1 photo from top level")
            assertEquals(2, photosFromGeneral.size, "Should have 2 photos from general")

            assertEquals(1, photosFromTopLevel[0].id, "Top level photo should have id=1")
            assertEquals(2, photosFromGeneral[0].id, "First general photo should have id=2")
            assertEquals(3, photosFromGeneral[1].id, "Second general photo should have id=3")
        }

    @Test
    fun `getMyCars should deduplicate photos by id when merging`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                        "photos": [
                            {
                                "id": 1,
                                "url": "http://155.212.170.94:9000/publicbct/cars/top1.jpg",
                                "uploadDate": "2026-01-04T19:23:58.147478Z",
                                "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                            }
                        ],
                        "general": {
                            "brandName": "Adam",
                            "modelName": "Revo",
                            "photos": [
                                {
                                    "id": 1,
                                    "url": "http://155.212.170.94:9000/publicbct/cars/duplicate.jpg",
                                    "uploadDate": "2026-01-04T19:24:26.05663Z",
                                    "carId": "a575c0b1-3736-475f-a4a8-5a87cfbdb18a"
                                }
                            ]
                        }
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = apiResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.my/api/Car/my")
            val result: List<CarItem> = response.parseResponse()

            assertNotNull(result)
            assertEquals(1, result.size)

            val car = result.first()
            val photosFromGeneral = car.general?.photos ?: emptyList()
            val photosFromTopLevel = car.photos

            assertEquals(1, photosFromTopLevel.size, "Should have 1 photo from top level")
            assertEquals(1, photosFromGeneral.size, "Should have 1 photo from general with same id")
            assertEquals(1, photosFromTopLevel[0].id, "Top level photo should have id=1")
            assertEquals(1, photosFromGeneral[0].id, "General photo should have id=1 (duplicate)")
        }
}
