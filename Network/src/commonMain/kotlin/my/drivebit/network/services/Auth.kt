package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.handleServiceError
import my.drivebit.network.parseResponse

interface Auth {
    suspend fun createOtp(login: String): CreateOtpResponse

    suspend fun login(
        login: String,
        password: String,
    ): AuthResponse

    suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse

    suspend fun changePasswordViaOtp(
        identifier: String,
        code: String,
        newPassword: String,
    ): AuthOperationResponse

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
data class LoginRequest(
    val login: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val accessToken: AccessTokenDTO,
    val refreshToken: RefreshTokenDTO,
    val success: Boolean,
    val error: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    val sessionId: String,
    val otp: String,
    val newPassword: String,
)

@Serializable
data class AuthOperationResponse(
    val success: Boolean,
    val error: String? = null,
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
        val url = "${DEFAULT_BASE_URL}Auth/create-otp"
        println("📡 [AuthService] createOtp called")
        println("   - URL: $url")
        println("   - Login: $login")

        return runCatching {
            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(CreateOtpRequest(login = login))
                    }

            println("📡 [AuthService] createOtp response received")
            val result: CreateOtpResponse = response.parseResponse()
            println("📡 [AuthService] createOtp successful")
            println("   - Session ID: ${result.sessionId}")
            println("   - Expires in: ${result.expiresIn}s")
            result
        }.handleServiceError("AuthService", "createOtp")
    }

    override suspend fun login(
        login: String,
        password: String,
    ): AuthResponse {
        val url = "${DEFAULT_BASE_URL}Auth/login"
        println("📡 [AuthService] login called")
        println("   - URL: $url")
        println("   - Login: $login")
        println("   - Password length: ${password.length}")

        return runCatching {
            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(login = login, password = password))
                    }

            println("📡 [AuthService] login response received")
            val result: AuthResponse = response.parseResponse()
            println("📡 [AuthService] login successful")
            println("   - Access token expires at: ${result.accessToken.expiresAt}")
            println("   - Refresh token expires at: ${result.refreshToken.expiresAt}")
            result
        }.handleServiceError("AuthService", "login")
    }

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse {
        val url = "${DEFAULT_BASE_URL}Auth/verify-otp"
        println("📡 [AuthService] verifyOtp called")
        println("   - URL: $url")
        println("   - Session ID: $identifier")
        println("   - OTP code length: ${code.length}")

        return runCatching {
            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(VerifyOtpRequest(sessionId = identifier, otp = code))
                    }

            println("📡 [AuthService] verifyOtp response received")
            val result: VerifyOtpResponse = response.parseResponse()
            println("📡 [AuthService] verifyOtp successful")
            println("   - Access token expires at: ${result.accessToken.expiresAt}")
            println("   - Refresh token expires at: ${result.refreshToken.expiresAt}")
            result
        }.handleServiceError("AuthService", "verifyOtp")
    }

    override suspend fun changePasswordViaOtp(
        identifier: String,
        code: String,
        newPassword: String,
    ): AuthOperationResponse {
        val url = "${DEFAULT_BASE_URL}Auth/change-password-via-otp"
        println("📡 [AuthService] changePasswordViaOtp called")
        println("   - URL: $url")
        println("   - Session ID: $identifier")
        println("   - OTP code length: ${code.length}")
        println("   - New password length: ${newPassword.length}")

        return runCatching {
            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(ChangePasswordRequest(sessionId = identifier, otp = code, newPassword = newPassword))
                    }

            println("📡 [AuthService] changePasswordViaOtp response received")
            val result: AuthOperationResponse = response.parseResponse()
            println("📡 [AuthService] changePasswordViaOtp successful")
            result
        }.handleServiceError("AuthService", "changePasswordViaOtp")
    }

    override suspend fun createTokens(refreshToken: String): CreateNewTokensResponse {
        val url = "${DEFAULT_BASE_URL}Auth/create-tokens"
        println("📡 [AuthService] createTokens called")
        println("   - URL: $url")
        println("   - Refresh token length: ${refreshToken.length}")
        println("   - Refresh token preview: ${refreshToken.take(20)}...")

        return runCatching {
            println("   - ✅ Using unauthorized HttpClient (NO Bearer token will be sent)")
            val response =
                httpClient
                    .post(url) {
                        contentType(ContentType.Application.Json)
                        // ⚠️ ВАЖНО: НЕ добавляем заголовок Authorization - refresh token идет в теле запроса
                        setBody(CreateNewTokensRequest(refreshToken = refreshToken))
                    }

            println("📡 [AuthService] Response received")
            val result: CreateNewTokensResponse = response.parseResponse()
            println("📡 [AuthService] Response parsed successfully")
            println("   - New access token expires at: ${result.accessToken.expiresAt}")
            println("   - New refresh token expires at: ${result.refreshToken.expiresAt}")
            result
        }.getOrElse { e ->
            println("❌ [AuthService] createTokens failed")
            println("   - Error type: ${e::class.simpleName}")
            println("   - Error message: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
