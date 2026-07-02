package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpResponseExtensionsTest {
    @Test
    fun `consumeMessageResponse throws when success is false`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = """{"success":false,"message":"нет валидированных документов"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val httpClient = HttpClient(mockEngine)

            val exception =
                assertFailsWith<NetworkException> {
                    httpClient.put("https://example.com/confirm").consumeMessageResponse()
                }

            assertEquals(HttpStatusCode.OK, exception.statusCode)
            assertEquals("нет валидированных документов", exception.message)
        }

    @Test
    fun `consumeMessageResponse succeeds when success is true`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = """{"success":true,"message":"OK"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val httpClient = HttpClient(mockEngine)

            httpClient.put("https://example.com/confirm").consumeMessageResponse()
        }

    @Test
    fun `extractHttpErrorMessage reads message field from validation response`() {
        val message =
            extractHttpErrorMessage(
                """{"message":"Недостаточно прав"}""",
            )
        assertEquals("Недостаточно прав", message)
    }
}
