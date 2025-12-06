package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import my.drivebit.network.services.Auth

expect fun createPlatformHttpClientEngine(): HttpClientEngine

expect fun getBaseUrl(): String

const val DEFAULT_BASE_URL = "https://drivebit.my/api/"

fun createHttpClientWithConfig(
    json: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
            explicitNulls = true
        },
    getToken: (() -> String),
    getRefreshToken: (() -> String),
    saveTokens: ((String?, String) -> Unit),
    authService: Auth,
): HttpClient =
    HttpClient(createPlatformHttpClientEngine()) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL
            logger =
                object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("🌐 [Ktor] $message")
                    }
                }
            filter { request ->
                request.url.host.contains("drivebit.my") || request.url.host.contains("api.drivebit.my")
            }
            sanitizeHeader { name -> name == "Authorization" }
        }

        install(Auth) {
            bearer {
                sendWithoutRequest { true }
                loadTokens {
                    val accessToken = getToken.invoke()
                    val refreshToken = getRefreshToken.invoke()
                    println("🔑 [Auth] loadTokens called")
                    println("   - Access token length: ${accessToken.length}")
                    println("   - Refresh token length: ${refreshToken.length}")
                    println(
                        "   - Access token: ${if (accessToken.isNotBlank()) {
                            "present (${accessToken.take(
                                20,
                            )}...)"
                        } else {
                            "empty or blank"
                        }}",
                    )
                    println(
                        "   - Refresh token: ${if (refreshToken.isNotBlank()) {
                            "present (${refreshToken.take(
                                20,
                            )}...)"
                        } else {
                            "empty or blank"
                        }}",
                    )
                    if (refreshToken.isNotEmpty() && refreshToken.isBlank()) {
                        println("   ⚠️ WARNING: Refresh token contains only whitespace!")
                    }
                    BearerTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )
                }
                refreshTokens {
                    println("🔄 [Auth] refreshTokens called")
                    val currentOldTokens = oldTokens
                    val refreshTokenValue = currentOldTokens?.refreshToken

                    println("🔄 [Auth] Old tokens:")
                    println("   - oldTokens is null: ${oldTokens == null}")
                    println("   - currentOldTokens is null: ${currentOldTokens == null}")
                    println(
                        "   - Access token: ${if (currentOldTokens?.accessToken != null) {
                            "present (${currentOldTokens.accessToken.take(
                                20,
                            )}...)"
                        } else {
                            "null"
                        }}",
                    )
                    println(
                        "   - Refresh token: ${if (refreshTokenValue != null && refreshTokenValue.isNotBlank()) {
                            "present (${refreshTokenValue.take(
                                20,
                            )}...)"
                        } else if (refreshTokenValue == null) {
                            "null"
                        } else {
                            "empty or blank"
                        }}",
                    )
                    println("   - Refresh token value length: ${refreshTokenValue?.length ?: 0}")
                    println("   - Refresh token isEmpty: ${refreshTokenValue?.isEmpty() ?: true}")
                    println("   - Refresh token isBlank: ${refreshTokenValue?.isBlank() ?: true}")
                    println("   - Refresh token trimmed length: ${refreshTokenValue?.trim()?.length ?: 0}")
                    if (refreshTokenValue != null && refreshTokenValue.length > 0) {
                        println("   - Refresh token first 30 chars: '${refreshTokenValue.take(30)}'")
                        println(
                            "   - Refresh token contains only spaces: ${refreshTokenValue.all { it.isWhitespace() }}",
                        )
                    }

                    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
                        println("❌ [Auth] Refresh token is null or blank, cannot refresh")
                        println("   - refreshTokenValue == null: ${refreshTokenValue == null}")
                        println("   - refreshTokenValue.isEmpty(): ${refreshTokenValue?.isEmpty() ?: "N/A"}")
                        println("   - refreshTokenValue.isBlank(): ${refreshTokenValue?.isBlank() ?: "N/A"}")
                        null
                    } else {
                        try {
                            println("🔄 [Auth] Attempting to refresh tokens...")
                            println("   - Using refresh token: ${refreshTokenValue.take(20)}...")

                            val newTokens = authService.createTokens(refreshTokenValue)

                            val newAccessToken = newTokens.accessToken.token
                            val newRefreshToken = newTokens.refreshToken.token

                            println("✅ [Auth] Tokens refreshed successfully")
                            println("   - New access token: ${newAccessToken.take(20)}...")
                            println("   - New refresh token: ${newRefreshToken.take(20)}...")

                            saveTokens.invoke(newAccessToken, newRefreshToken)

                            BearerTokens(
                                accessToken = newAccessToken,
                                refreshToken = newRefreshToken,
                            )
                        } catch (e: Exception) {
                            println("❌ [Auth] Failed to refresh tokens")
                            println("   - Error type: ${e::class.simpleName}")
                            println("   - Error message: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }
                }
            }
        }
    }
