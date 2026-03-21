@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Review {
    suspend fun createReview(request: CreateReviewRequest): CreatedReviewDTO
}

@Serializable
data class CreateReviewRequest(
    val carId: String,
    val stars: Int,
    val text: String? = null,
)

@Serializable
data class CreatedReviewDTO(
    @JsonNames("id", "Id") val id: String,
    @JsonNames("authorId", "AuthorId") val authorId: String? = null,
    @JsonNames("carId", "CarId") val carId: String? = null,
    @JsonNames("ownerId", "OwnerId") val ownerId: String? = null,
    @JsonNames("stars", "Stars") val stars: Int? = null,
    @JsonNames("text", "Text") val text: String? = null,
    @JsonNames("createdAt", "CreatedAt") val createdAt: String? = null,
    @JsonNames("updatedAt", "UpdatedAt") val updatedAt: String? = null,
)

class ReviewImpl(
    private val authorizedHttpClient: HttpClient,
) : Review {
    override suspend fun createReview(request: CreateReviewRequest): CreatedReviewDTO {
        val url = "${DEFAULT_BASE_URL}Review"
        val response =
            authorizedHttpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }
}
