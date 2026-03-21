@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse
import kotlin.math.min

interface Review {
    suspend fun createReview(request: CreateReviewRequest): CreatedReviewDTO

    suspend fun getReviewsByCarId(
        carId: String,
        page: Int,
        pageSize: Int,
        sortBy: String = "CreatedAt",
        sortOrder: String = "desc",
    ): ReviewListDTO
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

@Serializable
data class ReviewListDTO(
    @JsonNames("reviews", "Reviews") val reviews: List<ReviewListItemDTO> = emptyList(),
    @JsonNames("totalCount", "TotalCount") val totalCount: Int = 0,
    @JsonNames("page", "Page") val page: Int = 1,
    @JsonNames("pageSize", "PageSize") val pageSize: Int = 0,
    @JsonNames("totalPages", "TotalPages") val totalPages: Int = 0,
)

@Serializable
data class ReviewListItemDTO(
    @JsonNames("id", "Id") val id: String,
    @JsonNames("authorId", "AuthorId") val authorId: String? = null,
    @JsonNames("authorName", "AuthorName") val authorName: String? = null,
    @JsonNames("carId", "CarId") val carId: String? = null,
    @JsonNames("carModel", "CarModel") val carModel: String? = null,
    @JsonNames("stars", "Stars") val stars: Int = 0,
    @JsonNames("text", "Text") val text: String? = null,
    @JsonNames("createdAt", "CreatedAt") val createdAt: String? = null,
    @JsonNames("updatedAt", "UpdatedAt") val updatedAt: String? = null,
)

class ReviewImpl(
    private val unauthorizedHttpClient: HttpClient,
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

    override suspend fun getReviewsByCarId(
        carId: String,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
    ): ReviewListDTO {
        val url = "${DEFAULT_BASE_URL}Review/car/$carId"
        val response =
            unauthorizedHttpClient.get(url) {
                parameter("page", page)
                parameter("pageSize", min(pageSize, 30))
                parameter("sortBy", sortBy)
                parameter("sortOrder", sortOrder)
            }
        return response.parseResponse()
    }
}
