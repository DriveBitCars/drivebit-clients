package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Photo {
    suspend fun getAvatar(): AvatarResponse
}

@Serializable
data class AvatarResponse(
    val url: String,
)

class PhotoImpl(
    private val httpClient: HttpClient,
) : Photo {
    override suspend fun getAvatar(): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response = httpClient.get(url)
        return response.parseResponse()
    }
}

