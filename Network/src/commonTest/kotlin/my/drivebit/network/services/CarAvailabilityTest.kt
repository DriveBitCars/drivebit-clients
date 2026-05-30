package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarAvailabilityTest {
    @Test
    fun `getBlocks should deserialize public block with id and translations`() =
        runTest {
            val apiResponse =
                """
                [
                    {
                        "id": "11111111-1111-1111-1111-111111111111",
                        "startAt": "2026-06-10T00:00:00Z",
                        "endAt": "2026-06-13T00:00:00Z",
                        "blockType": "Maintenance",
                        "blockTypeTranslate": "Техническое обслуживание",
                        "status": "Active",
                        "statusTranslate": "Активен"
                    }
                ]
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/CarAvailability/car/test-car-id/blocks"))
                    assertEquals(HttpMethod.Get, request.method)
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

            val service = CarAvailabilityImpl(httpClient)
            val blocks = service.getBlocks("test-car-id")

            assertEquals(1, blocks.size)
            assertEquals("11111111-1111-1111-1111-111111111111", blocks[0].id)
            assertEquals("Maintenance", blocks[0].blockType)
            assertEquals("Техническое обслуживание", blocks[0].blockTypeTranslate)
        }

    @Test
    fun `createBlock should post request and return created block`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/CarAvailability/block"))
                    assertEquals(HttpMethod.Post, request.method)
                    respond(
                        content =
                            """
                            {
                                "id": "22222222-2222-2222-2222-222222222222",
                                "startAt": "2026-06-10T00:00:00Z",
                                "endAt": "2026-06-11T00:00:00Z",
                                "blockType": "Maintenance",
                                "blockTypeTranslate": "Техническое обслуживание"
                            }
                            """.trimIndent(),
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

            val service = CarAvailabilityImpl(httpClient)
            val block =
                service.createBlock(
                    CreateAvailabilityBlockRequest(
                        carId = "car-id",
                        startAt = "2026-06-10T00:00:00Z",
                        endAt = "2026-06-11T00:00:00Z",
                    ),
                )

            assertEquals("22222222-2222-2222-2222-222222222222", block.id)
            assertEquals("Maintenance", block.blockType)
        }

    @Test
    fun `deleteBlock should call delete endpoint`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/CarAvailability/block/block-id"))
                    assertEquals(HttpMethod.Delete, request.method)
                    respond(
                        content = """{"success":true}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient = HttpClient(mockEngine)
            val service = CarAvailabilityImpl(httpClient)
            service.deleteBlock("block-id")
        }
}
