package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse

interface CarAvailability {
    suspend fun getBlocks(
        carId: String,
        from: String? = null,
        to: String? = null,
    ): List<CarAvailabilityBlock>

    suspend fun createBlock(request: CreateAvailabilityBlockRequest): CarAvailabilityBlock

    suspend fun deleteBlock(blockId: String)
}

@Serializable
data class CarAvailabilityBlock(
    @JsonNames("id", "Id") val id: String = "",
    @JsonNames("startAt", "start_at", "StartAt") val startAt: String,
    @JsonNames("endAt", "end_at", "EndAt") val endAt: String,
    @JsonNames("blockType", "BlockType") val blockType: String? = null,
    @JsonNames("blockTypeTranslate", "BlockTypeTranslate") val blockTypeTranslate: String? = null,
    @JsonNames("reason", "Reason") val reason: String? = null,
)

@Serializable
data class CreateAvailabilityBlockRequest(
    @JsonNames("carId", "CarId") val carId: String,
    @JsonNames("startAt", "StartAt") val startAt: String,
    @JsonNames("endAt", "EndAt") val endAt: String,
    @JsonNames("blockType", "BlockType") val blockType: String = "Maintenance",
)

class CarAvailabilityImpl(
    private val httpClient: HttpClient,
) : CarAvailability {
    override suspend fun getBlocks(
        carId: String,
        from: String?,
        to: String?,
    ): List<CarAvailabilityBlock> {
        if (carId.isBlank()) {
            throw IllegalArgumentException("Car ID cannot be empty")
        }
        val url = "${DEFAULT_BASE_URL}CarAvailability/car/$carId/blocks"
        val response =
            httpClient.get(url) {
                from?.takeIf { it.isNotBlank() }?.let { parameter("from", it) }
                to?.takeIf { it.isNotBlank() }?.let { parameter("to", it) }
            }
        return response.parseResponse()
    }

    override suspend fun createBlock(request: CreateAvailabilityBlockRequest): CarAvailabilityBlock {
        val url = "${DEFAULT_BASE_URL}CarAvailability/block"
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }

    override suspend fun deleteBlock(blockId: String) {
        if (blockId.isBlank()) {
            throw IllegalArgumentException("Block ID cannot be empty")
        }
        val url = "${DEFAULT_BASE_URL}CarAvailability/block/$blockId"
        val response = httpClient.delete(url)
        response.consumeResponse()
    }
}
