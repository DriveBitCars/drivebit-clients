package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL

interface Auth {
    suspend fun createOtp(login: String): CreateOtpResponse

    suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse
}

@Serializable
data class CreateOtpRequest(
    val login: String,
)

@Serializable
data class CreateOtpResponse(
    val success: Boolean = true,
)

@Serializable
data class VerifyOtpRequest(
    val identifier: String,
    val code: String,
)

@Serializable
data class VerifyOtpResponse(
    val success: Boolean = true,
    val token: String? = null,
)

class AuthImpl(
    private val httpClient: HttpClient,
) : Auth {
    override suspend fun createOtp(login: String): CreateOtpResponse {
        val url = "${DEFAULT_BASE_URL}Auth/create-otp"
        val response =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(CreateOtpRequest(login = login))
                }
        return response.body<CreateOtpResponse>()
    }

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse {
        val url = "${DEFAULT_BASE_URL}Auth/verify-otp"
        val response =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyOtpRequest(identifier = identifier, code = code))
                }
        return response.body<VerifyOtpResponse>()
    }
}
