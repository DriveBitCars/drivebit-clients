package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface CarAvailability {
    suspend fun getBlocks(carId: String): List<CarAvailabilityBlock>
}

@Serializable
data class CarAvailabilityBlock(
    @JsonNames("startAt", "start_at", "StartAt") val startAt: String,
    @JsonNames("endAt", "end_at", "EndAt") val endAt: String,
)

class CarAvailabilityImpl(
    private val httpClient: HttpClient,
) : CarAvailability {
    override suspend fun getBlocks(carId: String): List<CarAvailabilityBlock> {
        if (carId.isBlank()) {
            throw IllegalArgumentException("Car ID cannot be empty")
        }
        val url = "${DEFAULT_BASE_URL}CarAvailability/car/$carId/blocks"
        val response = httpClient.get(url)
        return response.parseResponse()
    }
}
