package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PaymentRegisterBookingTest {
    @Test
    fun `registerBookingPayment returns Redirect when 200 with paymentUrl`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.contains(bookingId))
                    assertTrue(request.url.parameters["returnUrl"] == "https://example.com/ok")
                    assertTrue(request.url.parameters["failUrl"] == "https://example.com/fail")
                    respond(
                        content = """{"paymentUrl":"https://pay.example/checkout"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client = HttpClient(mockEngine)
            val payment = PaymentImpl(client)
            val result =
                payment.registerBookingPayment(
                    bookingId = bookingId,
                    returnUrl = "https://example.com/ok",
                    failUrl = "https://example.com/fail",
                )
            val redirect = assertIs<PayBookingResult.Redirect>(result)
            assertEquals("https://pay.example/checkout", redirect.url)
        }

    @Test
    fun `registerBookingPayment returns AlreadyPaid when 208`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = """{"message":"Уже оплачено"}""",
                        status = HttpStatusCode(208, "Already Reported"),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val payment = PaymentImpl(HttpClient(mockEngine))
            val result =
                payment.registerBookingPayment(
                    bookingId = "550e8400-e29b-41d4-a716-446655440000",
                    returnUrl = "https://a",
                    failUrl = "https://b",
                )
            val already = assertIs<PayBookingResult.AlreadyPaid>(result)
            assertEquals("Уже оплачено", already.message)
        }

    @Test
    fun `registerBookingPayment returns Failed on 400 with message`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = """{"message":"Нельзя оплатить"}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val payment = PaymentImpl(HttpClient(mockEngine))
            val result =
                payment.registerBookingPayment(
                    bookingId = "550e8400-e29b-41d4-a716-446655440000",
                    returnUrl = "https://a",
                    failUrl = "https://b",
                )
            val failed = assertIs<PayBookingResult.Failed>(result)
            assertEquals("Нельзя оплатить", failed.message)
        }

    @Test
    fun `registerBookingPrepayment hits prepay endpoint`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/prepay"))
                    respond(
                        content = """{"paymentUrl":"https://pay.example/prepay"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val payment = PaymentImpl(HttpClient(mockEngine))
            val result =
                payment.registerBookingPrepayment(
                    bookingId = bookingId,
                    returnUrl = "https://example.com/ok",
                    failUrl = "https://example.com/fail",
                )
            val redirect = assertIs<PayBookingResult.Redirect>(result)
            assertEquals("https://pay.example/prepay", redirect.url)
        }
}
