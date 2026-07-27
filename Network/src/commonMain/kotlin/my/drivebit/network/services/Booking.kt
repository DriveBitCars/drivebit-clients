package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.ValidationErrorResponse
import my.drivebit.network.collectErrorMessages
import my.drivebit.network.consumeMessageResponse
import my.drivebit.network.defaultJson
import my.drivebit.network.parseResponse
import my.drivebit.utils.resolveMinioImageUrlForBrowser

interface Booking {
    suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse

    suspend fun getMyAsRenter(): List<BookingDTO>

    suspend fun getMyAsOwner(): List<BookingDTO>

    suspend fun getById(bookingId: String): BookingDTO

    suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO

    suspend fun confirmAsOwner(bookingId: String)

    suspend fun declineAsOwner(bookingId: String)

    suspend fun signContractAsOwner(bookingId: String): BookingDTO

    suspend fun signContractAsRenter(bookingId: String): BookingDTO

    suspend fun getContract(bookingId: String): GetBookingContractResult
}

sealed class GetBookingContractResult {
    data class Success(
        val contract: BookingContractDownloadDto,
    ) : GetBookingContractResult()

    data class DataIncomplete(
        val message: String,
        val reasons: List<String>,
    ) : GetBookingContractResult()

    data class Failed(
        val message: String,
    ) : GetBookingContractResult()
}

@Serializable
data class CheckBookingAvailabilityRequest(
    val carId: String,
    val startAt: String,
    val endAt: String,
)

@Serializable
data class CheckBookingAvailabilityResponse(
    val isAvailable: Boolean,
    val conflict: String? = null,
    @JsonNames("estimatedPrice", "estimated_price") val estimatedPrice: Double? = null,
    @JsonNames("pricePerDay", "price_per_day") val pricePerDay: Double? = null,
    @JsonNames("totalPrice", "total_price") val totalPrice: Double? = null,
    @JsonNames("estimatedDeposit", "estimated_deposit") val estimatedDeposit: Double? = null,
    @JsonNames("prepaymentPercent", "prepayment_percent") val prepaymentPercent: Double? = null,
    @JsonNames("estimatedPrepayment", "estimated_prepayment") val estimatedPrepayment: Double? = null,
    @JsonNames("balanceDueAmount", "balance_due_amount") val balanceDueAmount: Double? = null,
    @JsonNames("prepaymentAvailable", "prepayment_available") val prepaymentAvailable: Boolean = false,
)

@Serializable
data class BookingDTO(
    val id: String,
    val carId: String,
    val carBrandName: String? = null,
    val carModelName: String? = null,
    val carLicensePlate: String? = null,
    val renterId: String,
    val renterName: String? = null,
    val renterPhone: String? = null,
    val ownerId: String,
    val ownerName: String? = null,
    val ownerPhone: String? = null,
    val startAt: String,
    val endAt: String,
    val totalAmount: Double,
    val deposit: Double = 0.0,
    @JsonNames("totalAmountWithDeposit", "total_amount_with_deposit") val totalAmountWithDeposit: Double = 0.0,
    @JsonNames("prepaymentPercent", "prepayment_percent") val prepaymentPercent: Double = 0.0,
    @JsonNames("prepaymentAmount", "prepayment_amount") val prepaymentAmount: Double = 0.0,
    @JsonNames("remainingRentalAmount", "remaining_rental_amount") val remainingRentalAmount: Double = 0.0,
    @JsonNames("balanceDueAmount", "balance_due_amount") val balanceDueAmount: Double = 0.0,
    @JsonNames("prepaymentPaidAt", "prepayment_paid_at") val prepaymentPaidAt: String? = null,
    @JsonNames("prepaymentAvailable", "prepayment_available") val prepaymentAvailable: Boolean = false,
    @JsonNames("canPayPrepayment", "can_pay_prepayment") val canPayPrepayment: Boolean = false,
    @JsonNames("canPayFullAmount", "can_pay_full_amount") val canPayFullAmount: Boolean = false,
    @JsonNames("contractSignedByOwner", "contract_signed_by_owner") val contractSignedByOwner: Boolean = false,
    @JsonNames("contractSignedByRenter", "contract_signed_by_renter") val contractSignedByRenter: Boolean = false,
    @JsonNames("canSignContractAsOwner", "can_sign_contract_as_owner") val canSignContractAsOwner: Boolean = false,
    @JsonNames("canSignContractAsRenter", "can_sign_contract_as_renter") val canSignContractAsRenter: Boolean = false,
    @JsonNames("contractSignedByOwnerAt", "contract_signed_by_owner_at") val contractSignedByOwnerAt: String? = null,
    @JsonNames("contractSignedByRenterAt", "contract_signed_by_renter_at") val contractSignedByRenterAt: String? = null,
    @JsonNames("isOwnerVerified", "is_owner_verified") val isOwnerVerified: Boolean = false,
    @JsonNames("isRenterVerified", "is_renter_verified") val isRenterVerified: Boolean = false,
    @JsonNames("isCarVerified", "is_car_verified") val isCarVerified: Boolean = false,
    @JsonNames("dailyRate", "daily_rate") val dailyRate: Double = 0.0,
    val status: String,
    val statusTranslate: String? = null,
    val createdAt: String,
    val comment: String? = null,
)

enum class BookingCheckoutKind {
    Prepayment,
    FullOrBalance,
}

fun BookingDTO.statusAllowsRenterPayment(): Boolean = canPayFullAmount

fun BookingDTO.renterFullOrBalanceAmountRub(): Int {
    val amount =
        if (prepaymentPaidAt != null) {
            balanceDueAmount
        } else if (totalAmountWithDeposit > 0) {
            totalAmountWithDeposit
        } else {
            totalAmount + deposit
        }
    return amount.roundToRubles()
}

fun BookingDTO.renterFullOrBalancePaymentLabel(): String =
    if (prepaymentPaidAt != null) {
        "Оплатить остаток"
    } else {
        "Оплатить полностью"
    }

fun BookingDTO.prepaymentAmountRub(): Int = prepaymentAmount.roundToRubles()

fun BookingDTO.prepaymentButtonLabel(): String = "Предоплата (${prepaymentAmountRub()} ₽)"

private fun Double.roundToRubles(): Int = kotlin.math.round(this).toInt()

@Serializable
data class CreateBookingRequest(
    val carId: String,
    val startAt: String,
    val endAt: String,
    val comment: String? = null,
)

@Serializable
data class BookingContractUnavailableDto(
    val errorCode: String? = null,
    val message: String? = null,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class BookingContractDownloadDto(
    @JsonNames("contractNumber", "contract_number") val contractNumber: Long,
    val fileName: String,
    @JsonNames("downloadUrl", "download_url") val downloadUrl: String,
    @JsonNames("urlExpiresAt", "url_expires_at") val urlExpiresAt: String,
    @JsonNames("generatedAt", "generated_at") val generatedAt: String,
)

fun BookingContractDownloadDto.browserDownloadUrl(): String = resolveMinioImageUrlForBrowser(downloadUrl) ?: downloadUrl

fun BookingDTO.statusAllowsContractDownload(): Boolean =
    status.equals("Confirmed", ignoreCase = true) ||
        status.equals("PrePaid", ignoreCase = true) ||
        status.equals("Paid", ignoreCase = true) ||
        status.equals("ContractSignedByOwner", ignoreCase = true) ||
        status.equals("ContractSignedByRenter", ignoreCase = true) ||
        status.equals("ContractSignedByBoth", ignoreCase = true) ||
        status.equals("Active", ignoreCase = true) ||
        status.equals("Completed", ignoreCase = true)

fun BookingDTO.canShowSignContractAsOwner(): Boolean =
    canSignContractAsOwner ||
        (statusAllowsContractSignUi() && !contractSignedByOwner)

fun BookingDTO.canShowSignContractAsRenter(): Boolean =
    canSignContractAsRenter ||
        (statusAllowsContractSignUi() && !contractSignedByRenter)

private fun BookingDTO.statusAllowsContractSignUi(): Boolean =
    status.equals("Confirmed", ignoreCase = true) ||
        status.equals("PrePaid", ignoreCase = true) ||
        status.equals("Paid", ignoreCase = true) ||
        status.equals("ContractSignedByOwner", ignoreCase = true) ||
        status.equals("ContractSignedByRenter", ignoreCase = true)

fun BookingDTO.canShowSignContractInChat(counterpartyUserId: String): Boolean =
    when (counterpartyUserId) {
        ownerId -> canShowSignContractAsRenter()
        renterId -> canShowSignContractAsOwner()
        else -> false
    }

enum class SignContractChatRole {
    Owner,
    Renter,
}

fun BookingDTO.signContractChatRole(counterpartyUserId: String): SignContractChatRole? =
    when (counterpartyUserId) {
        ownerId -> if (canShowSignContractAsRenter()) SignContractChatRole.Renter else null
        renterId -> if (canShowSignContractAsOwner()) SignContractChatRole.Owner else null
        else -> null
    }

fun BookingDTO.isTerminalRenterBooking(): Boolean =
    status.equals("Completed", ignoreCase = true) ||
        status.equals("Cancelled", ignoreCase = true) ||
        status.equals("Canceled", ignoreCase = true) ||
        status.equals("Declined", ignoreCase = true)

class BookingImpl(
    private val unauthorizedHttpClient: HttpClient,
    private val authorizedHttpClient: HttpClient,
) : Booking {
    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse {
        val url = "${DEFAULT_BASE_URL}Booking/calculate"
        val response =
            unauthorizedHttpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }

    override suspend fun getMyAsRenter(): List<BookingDTO> {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-renter"
        val response = authorizedHttpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getMyAsOwner(): List<BookingDTO> {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-owner/list"
        val response = authorizedHttpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getById(bookingId: String): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/$bookingId"
        val response = authorizedHttpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-renter"
        val response =
            authorizedHttpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }

    override suspend fun confirmAsOwner(bookingId: String) {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-owner/$bookingId/confirm"
        val response = authorizedHttpClient.put(url) { }
        response.consumeMessageResponse(defaultError = "Не удалось подтвердить сделку")
    }

    override suspend fun declineAsOwner(bookingId: String) {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-owner/$bookingId/decline"
        val response = authorizedHttpClient.put(url) { }
        response.consumeMessageResponse(defaultError = "Не удалось отклонить сделку")
    }

    override suspend fun signContractAsOwner(bookingId: String): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/$bookingId/sign-contract-as-owner"
        val response = authorizedHttpClient.post(url) { }
        return response.parseResponse()
    }

    override suspend fun signContractAsRenter(bookingId: String): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/$bookingId/sign-contract-as-renter"
        val response = authorizedHttpClient.post(url) { }
        return response.parseResponse()
    }

    override suspend fun getContract(bookingId: String): GetBookingContractResult {
        val url = "${DEFAULT_BASE_URL}Booking/$bookingId/contract"
        val response = authorizedHttpClient.get(url)
        val bodyString = response.bodyAsText()
        return when {
            response.status.isSuccess() -> {
                val contract =
                    runCatching {
                        defaultJson.decodeFromString(BookingContractDownloadDto.serializer(), bodyString)
                    }.getOrElse {
                        return GetBookingContractResult.Failed("Не удалось разобрать ответ сервера")
                    }
                GetBookingContractResult.Success(contract)
            }
            response.status == HttpStatusCode.UnprocessableEntity -> {
                val unavailable =
                    runCatching {
                        defaultJson.decodeFromString(BookingContractUnavailableDto.serializer(), bodyString)
                    }.getOrElse {
                        return GetBookingContractResult.Failed(
                            extractBookingContractErrorMessage(bodyString, defaultJson),
                        )
                    }
                GetBookingContractResult.DataIncomplete(
                    message =
                        unavailable.message?.takeIf { it.isNotBlank() }
                            ?: "Невозможно скачать договор: не заполнены обязательные данные.",
                    reasons = unavailable.reasons.filter { it.isNotBlank() },
                )
            }
            else ->
                GetBookingContractResult.Failed(extractBookingContractErrorMessage(bodyString, defaultJson))
        }
    }
}

private fun extractBookingContractErrorMessage(
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
    }.ifBlank { "Не удалось загрузить договор" }
}
