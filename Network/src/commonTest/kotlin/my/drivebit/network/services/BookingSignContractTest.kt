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
    fun `signContractAsOwner parses full production response`() =
        runTest {
            val bookingId = "8a2f5e6c-e87e-473e-b574-ce5cbbe62f57"
            val mockEngine =
                MockEngine { request ->
                    respond(
                        content =
                            """
                            {
                              "id": "$bookingId",
                              "carId": "f87275aa-b242-420b-99c0-dd4c8e0fae5c",
                              "carBrandName": "УАЗ",
                              "carModelName": "Patriot",
                              "carLicensePlate": "К123КК77",
                              "renterId": "bf0ad237-da64-4542-8779-a803d6c7d3cc",
                              "renterName": "Бутов Антон Петрович",
                              "ownerId": "ef2d17e8-0f26-4e20-af58-4da0f1228425",
                              "ownerName": "Бутов Антон Петрович",
                              "startAt": "2026-06-07T08:19:00Z",
                              "endAt": "2026-06-08T08:19:00Z",
                              "totalAmount": 10,
                              "deposit": 5,
                              "totalAmountWithDeposit": 15,
                              "prepaymentPercent": 10,
                              "prepaymentAmount": 1,
                              "remainingRentalAmount": 9,
                              "balanceDueAmount": 14,
                              "prepaymentPaidAt": "2026-06-08T06:37:09.21638Z",
                              "prepaymentAvailable": true,
                              "canPayPrepayment": false,
                              "canPayFullAmount": false,
                              "contractSignedByOwner": true,
                              "contractSignedByRenter": true,
                              "canSignContractAsOwner": false,
                              "canSignContractAsRenter": false,
                              "contractSignedByOwnerAt": "2026-06-08T06:39:29.140777Z",
                              "contractSignedByRenterAt": "2026-06-08T06:38:54.054968Z",
                              "dailyRate": 10,
                              "status": "ContractSignedByBoth",
                              "statusTranslate": "Контракт подписан обоими сторонами",
                              "createdAt": "2026-06-07T07:19:58.80443Z",
                              "comment": null
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))
            val result = booking.signContractAsOwner(bookingId)

            assertEquals("ContractSignedByBoth", result.status)
            assertTrue(result.contractSignedByOwner)
            assertTrue(result.contractSignedByRenter)
        }

    @Test
    fun `signContractAsOwner parses verification badge aggregates`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            val mockEngine =
                MockEngine {
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
                              "status": "Paid",
                              "createdAt": "2026-05-27T10:00:00Z",
                              "isOwnerVerified": true,
                              "isRenterVerified": true,
                              "isCarVerified": false
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val result = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine)).signContractAsOwner(bookingId)

            assertTrue(result.isOwnerVerified)
            assertTrue(result.isRenterVerified)
            assertEquals(false, result.isCarVerified)
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
