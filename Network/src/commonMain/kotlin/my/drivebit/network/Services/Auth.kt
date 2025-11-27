package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.getBaseUrl
import my.drivebit.network.parseResponse

interface Auth {
    suspend fun createOtp(login: String): CreateOtpResponse

    suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse

    suspend fun createTokens(refreshToken: String): CreateNewTokensResponse
}

@Serializable
data class CreateOtpRequest(
    val login: String,
)

@Serializable
data class CreateOtpResponse(
    val message: String? = null,
    val sessionId: String,
    val expiresIn: Int,
)

@Serializable
data class VerifyOtpRequest(
    val sessionId: String,
    val otp: String,
)

@Serializable
data class AccessTokenDTO(
    val token: String,
    val expiresAt: String,
)

@Serializable
data class RefreshTokenDTO(
    val token: String,
    val userId: String,
    val expiresAt: String,
    val createdAt: String,
)

@Serializable
data class VerifyOtpResponse(
    val accessToken: AccessTokenDTO,
    val refreshToken: RefreshTokenDTO,
)

@Serializable
data class CreateNewTokensRequest(
    val refreshToken: String,
)

@Serializable
data class CreateNewTokensResponse(
    val refreshToken: RefreshTokenDTO,
    val accessToken: AccessTokenDTO,
)

class AuthImpl(
    private val httpClient: HttpClient,
) : Auth {
    override suspend fun createOtp(login: String): CreateOtpResponse {
        val url = "${getBaseUrl()}Auth/create-otp"
        val response =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(CreateOtpRequest(login = login))
                }

        return response.parseResponse()
    }

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse {
        val url = "${getBaseUrl()}Auth/verify-otp"
        val response =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyOtpRequest(sessionId = identifier, otp = code))
                }

        return response.parseResponse()
    }

    override suspend fun createTokens(refreshToken: String): CreateNewTokensResponse {
        val url = "${getBaseUrl()}Auth/create-tokens"
        val response =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(CreateNewTokensRequest(refreshToken = refreshToken))
                }

        return response.parseResponse()
    }
}
