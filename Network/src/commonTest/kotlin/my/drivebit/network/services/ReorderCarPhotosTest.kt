package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReorderCarPhotosTest {
    @Test
    fun `reorderCarPhotos puts photoIds and returns sorted photos`() =
        runTest {
            var capturedMethod: HttpMethod? = null
            var capturedPath: String? = null
            var capturedBody: String? = null
            val carId = "11111111-1111-1111-1111-111111111111"
            val mockEngine =
                MockEngine { request ->
                    capturedMethod = request.method
                    capturedPath = request.url.encodedPath
                    capturedBody = (request.body as TextContent).text
                    respond(
                        content =
                            """
                            [
                              {"id":2,"url":"https://cdn.example/b.jpg","uploadDate":"2026-01-01","sortOrder":1,"thumbnailUrl":null},
                              {"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":2,"thumbnailUrl":null}
                            ]
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
            val photo = PhotoImpl(httpClient)

            val result = photo.reorderCarPhotos(carId, listOf(2, 1))

            assertEquals(HttpMethod.Put, capturedMethod)
            assertTrue(capturedPath!!.endsWith("/Photo/car/my/$carId/order"))
            val bodyJson =
                Json { ignoreUnknownKeys = true }
                    .decodeFromString(ReorderCarPhotosRequest.serializer(), capturedBody!!)
            assertEquals(listOf(2, 1), bodyJson.photoIds)
            assertEquals(listOf(2, 1), result.map { it.id })
            assertEquals(listOf(1, 2), result.map { it.sortOrder })
        }
}
