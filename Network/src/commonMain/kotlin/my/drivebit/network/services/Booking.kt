package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Booking {
    suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse

    suspend fun getMyAsRenter(): List<BookingDTO>

    suspend fun getMyAsOwner(): List<BookingDTO>

    suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO

    suspend fun confirmAsOwner(bookingId: String): BookingDTO

    suspend fun declineAsOwner(bookingId: String): BookingDTO
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
    val estimatedPrice: Double? = null,
)

@Serializable
data class BookingDTO(
    val id: String,
    val carId: String,
    val carBrandName: String? = null,
    val carModelName: String? = null,
    val renterId: String,
    val renterName: String? = null,
    val renterPhone: String? = null,
    val ownerId: String,
    val ownerName: String? = null,
    val ownerPhone: String? = null,
    val startAt: String,
    val endAt: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val comment: String? = null,
)

@Serializable
data class CreateBookingRequest(
    val carId: String,
    val startAt: String,
    val endAt: String,
    val comment: String? = null,
)

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

    override suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-renter"
        val response =
            authorizedHttpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }

    override suspend fun confirmAsOwner(bookingId: String): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-owner/$bookingId/confirm"
        val response = authorizedHttpClient.put(url) { }
        return response.parseResponse()
    }

    override suspend fun declineAsOwner(bookingId: String): BookingDTO {
        val url = "${DEFAULT_BASE_URL}Booking/my/as-owner/$bookingId/decline"
        val response = authorizedHttpClient.put(url) { }
        return response.parseResponse()
    }
}
