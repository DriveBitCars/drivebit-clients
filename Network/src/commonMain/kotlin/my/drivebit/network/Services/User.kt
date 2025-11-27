package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import my.drivebit.network.getBaseUrl
import my.drivebit.network.parseResponse

interface User {
    suspend fun userGet(): UserGetResponse
}

@Serializable
data class UserGetResponse(
    val id: String,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val email: String? = null,
    val createdAt: String,
    val photos: List<String> = emptyList(),
)

class UserImpl(
    private val httpClient: HttpClient,
) : User {
    override suspend fun userGet(): UserGetResponse {
        val url = "${getBaseUrl()}User/user-get"
        val response = httpClient.get(url)

        return response.parseResponse()
    }
}
