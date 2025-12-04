package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.*
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
            level = LogLevel.INFO
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = getToken.invoke()
                    val refreshToken = getRefreshToken.invoke()
                    BearerTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )
                }
                refreshTokens {
                    val refreshTokenValue = oldTokens?.refreshToken!!
                    try {
                        val newTokens = authService.createTokens(refreshTokenValue)
                        val newAccessToken = newTokens.accessToken.token
                        val newRefreshToken = newTokens.refreshToken.token

                        saveTokens.invoke(newAccessToken, newRefreshToken)
                        BearerTokens(
                            accessToken = newAccessToken,
                            refreshToken = newRefreshToken,
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
