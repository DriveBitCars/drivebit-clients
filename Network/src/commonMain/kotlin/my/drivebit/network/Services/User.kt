package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface User {
    suspend fun userGet(): UserGetResponse

    suspend fun updateUser(
        firstName: String?,
        lastName: String?,
        middleName: String?,
    ): UserGetResponse
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

@Serializable
data class UserEditRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
)

class UserImpl(
    private val httpClient: HttpClient,
) : User {
    override suspend fun userGet(): UserGetResponse {
        val url = "${DEFAULT_BASE_URL}User/user"
        val response = httpClient.get(url)

        return response.parseResponse()
    }

    override suspend fun updateUser(
        firstName: String?,
        lastName: String?,
        middleName: String?,
    ): UserGetResponse {
        val url = "${DEFAULT_BASE_URL}User/user"
        val request = UserEditRequest(firstName = firstName, lastName = lastName, middleName = middleName)
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        return response.parseResponse()
    }
}
