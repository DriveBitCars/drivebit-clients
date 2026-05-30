package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.ValidationErrorResponse
import my.drivebit.network.collectErrorMessages
import my.drivebit.network.defaultJson

interface Payment {
    suspend fun registerBookingPayment(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ): PayBookingResult

    suspend fun registerBookingPrepayment(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ): PayBookingResult
}

sealed class PayBookingResult {
    data class Redirect(
        val url: String,
    ) : PayBookingResult()

    data class AlreadyPaid(
        val message: String? = null,
    ) : PayBookingResult()

    data class Failed(
        val message: String,
    ) : PayBookingResult()
}

@Serializable
private data class RegisterPaymentResponse(
    val paymentUrl: String? = null,
)

@Serializable
private data class PaymentAlreadyReportedBody(
    val message: String? = null,
)

class PaymentImpl(
    private val httpClient: HttpClient,
) : Payment {
    override suspend fun registerBookingPayment(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ): PayBookingResult =
        httpClient.registerBookingCheckout(
            path = "${DEFAULT_BASE_URL}Payment/booking/$bookingId/pay",
            returnUrl = returnUrl,
            failUrl = failUrl,
        )

    override suspend fun registerBookingPrepayment(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ): PayBookingResult =
        httpClient.registerBookingCheckout(
            path = "${DEFAULT_BASE_URL}Payment/booking/$bookingId/prepay",
            returnUrl = returnUrl,
            failUrl = failUrl,
        )
}

suspend fun Payment.checkoutBooking(
    bookingId: String,
    kind: BookingCheckoutKind,
    returnUrl: String,
    failUrl: String,
): PayBookingResult =
    when (kind) {
        BookingCheckoutKind.Prepayment -> registerBookingPrepayment(bookingId, returnUrl, failUrl)
        BookingCheckoutKind.FullOrBalance -> registerBookingPayment(bookingId, returnUrl, failUrl)
    }

private suspend fun HttpClient.registerBookingCheckout(
    path: String,
    returnUrl: String,
    failUrl: String,
): PayBookingResult {
    val response =
        post(path) {
            parameter("returnUrl", returnUrl)
            parameter("failUrl", failUrl)
        }
    val bodyString = response.bodyAsText()
    return when {
        response.status == HttpStatusCode.OK -> {
            val dto =
                runCatching {
                    defaultJson.decodeFromString(RegisterPaymentResponse.serializer(), bodyString)
                }.getOrElse {
                    return PayBookingResult.Failed("Не удалось разобрать ответ оплаты")
                }
            val url = dto.paymentUrl?.takeIf { it.isNotBlank() }
            if (url != null) {
                PayBookingResult.Redirect(url)
            } else {
                PayBookingResult.Failed("Пустая ссылка на оплату")
            }
        }
        response.status.value == 208 -> {
            val msg =
                runCatching {
                    defaultJson.decodeFromString(PaymentAlreadyReportedBody.serializer(), bodyString).message
                }.getOrNull()
            PayBookingResult.AlreadyPaid(message = msg)
        }
        !response.status.isSuccess() -> {
            PayBookingResult.Failed(extractPaymentErrorMessage(bodyString, defaultJson))
        }
        else -> {
            PayBookingResult.Failed("Неожиданный ответ сервера: ${response.status}")
        }
    }
}

private fun extractPaymentErrorMessage(
    bodyString: String,
    json: Json,
): String {
    val trimmedBody = bodyString.trim()
    return runCatching {
        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
            val errorResponse = json.decodeFromString(ValidationErrorResponse.serializer(), trimmedBody)
            val errorMessages = errorResponse.errors?.collectErrorMessages().orEmpty()
            when {
                !errorResponse.message.isNullOrBlank() -> errorResponse.message
                errorMessages.isNotEmpty() -> errorMessages.joinToString(". ")
                errorResponse.detail != null -> errorResponse.detail
                errorResponse.error != null -> errorResponse.error
                errorResponse.title != null -> errorResponse.title
                else -> trimmedBody.trim('"').trim()
            }
        } else {
            trimmedBody.trim('"').trim()
        }
    }.getOrElse {
        trimmedBody.trim('"').trim()
    }
}
