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

const val DEFAULT_BASE_URL = "https://drivebit.ru/api/"

private val tokenRefreshCoordinator = TokenRefreshCoordinator()

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
    onRefreshFailed: () -> Unit = {},
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
                request.url.host.contains("drivebit.ru") || request.url.host.contains("api.drivebit.ru")
            }
        }

        install(Auth) {
            bearer {
                sendWithoutRequest { true }
                loadTokens {
                    BearerTokens(
                        accessToken = getToken.invoke(),
                        refreshToken = getRefreshToken.invoke(),
                    )
                }
                refreshTokens {
                    tokenRefreshCoordinator.refreshTokens(
                        staleRefreshToken = oldTokens?.refreshToken,
                        getAccessToken = getToken,
                        getRefreshToken = getRefreshToken,
                        saveTokens = saveTokens,
                        requestRefresh = { refreshToken ->
                            val newTokens = authService.createTokens(refreshToken)
                            newTokens.accessToken.token to newTokens.refreshToken.token
                        },
                        onRefreshFailed = onRefreshFailed,
                    )
                }
            }
        }
    }
