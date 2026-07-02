package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BookingConfirmTest {
    @Test
    fun `confirmAsOwner throws when api returns success false`() =
        runTest {
            val bookingId = "c5db17ad-d87f-436f-ae25-12213f114667"
            var requestedPath: String? = null
            val mockEngine =
                MockEngine { request ->
                    requestedPath = request.url.encodedPath
                    assertEquals(HttpMethod.Put, request.method)
                    respond(
                        content = """{"success":false,"message":"нет валидированных документов"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))

            val exception =
                assertFailsWith<NetworkException> {
                    booking.confirmAsOwner(bookingId)
                }

            assertTrue(requestedPath!!.endsWith("/Booking/my/as-owner/$bookingId/confirm"))
            assertEquals("нет валидированных документов", exception.message)
        }
}
