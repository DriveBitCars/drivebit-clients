package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookingSignContractTest {
    @Test
    fun `signContractAsOwner posts to owner endpoint and parses response`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            var requestedPath: String? = null
            val mockEngine =
                MockEngine { request ->
                    requestedPath = request.url.encodedPath
                    assertEquals(HttpMethod.Post, request.method)
                    respond(
                        content =
                            """
                            {
                              "id": "$bookingId",
                              "carId": "660e8400-e29b-41d4-a716-446655440001",
                              "renterId": "770e8400-e29b-41d4-a716-446655440002",
                              "ownerId": "880e8400-e29b-41d4-a716-446655440003",
                              "startAt": "2026-05-28T10:00:00Z",
                              "endAt": "2026-05-30T10:00:00Z",
                              "totalAmount": 10000.0,
                              "status": "ContractSignedByOwner",
                              "canSignContractAsOwner": false,
                              "canSignContractAsRenter": true,
                              "contractSignedByOwner": true,
                              "createdAt": "2026-05-27T10:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))
            val result = booking.signContractAsOwner(bookingId)

            assertTrue(requestedPath!!.endsWith("/Booking/$bookingId/sign-contract-as-owner"))
            assertEquals("ContractSignedByOwner", result.status)
            assertTrue(result.contractSignedByOwner)
            assertTrue(result.canSignContractAsRenter)
        }

    @Test
    fun `signContractAsRenter posts to renter endpoint and parses response`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            var requestedPath: String? = null
            val mockEngine =
                MockEngine { request ->
                    requestedPath = request.url.encodedPath
                    assertEquals(HttpMethod.Post, request.method)
                    respond(
                        content =
                            """
                            {
                              "id": "$bookingId",
                              "carId": "660e8400-e29b-41d4-a716-446655440001",
                              "renterId": "770e8400-e29b-41d4-a716-446655440002",
                              "ownerId": "880e8400-e29b-41d4-a716-446655440003",
                              "startAt": "2026-05-28T10:00:00Z",
                              "endAt": "2026-05-30T10:00:00Z",
                              "totalAmount": 10000.0,
                              "status": "ContractSignedByRenter",
                              "canSignContractAsRenter": false,
                              "contractSignedByRenter": true,
                              "createdAt": "2026-05-27T10:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))
            val result = booking.signContractAsRenter(bookingId)

            assertTrue(requestedPath!!.endsWith("/Booking/$bookingId/sign-contract-as-renter"))
            assertEquals("ContractSignedByRenter", result.status)
            assertTrue(result.contractSignedByRenter)
        }
}
