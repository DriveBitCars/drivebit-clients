package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClientEngine(): HttpClientEngine

const val DEFAULT_BASE_URL = "http://api.drivebit.my:5000/"

class AuthInterceptorConfig {
    var tokenProvider: (() -> String?)? = null
}

val AuthInterceptorPlugin =
    createClientPlugin("AuthInterceptor", ::AuthInterceptorConfig) {
        val tokenProvider = pluginConfig.tokenProvider

        onRequest { request, _ ->
            tokenProvider?.invoke()?.let { token ->
                request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

fun createHttpClientWithConfig(
    json: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        },
    getToken: (() -> String?)? = null,
): HttpClient =
    HttpClient(createPlatformHttpClientEngine()) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }

        if (getToken != null) {
            install(AuthInterceptorPlugin) {
                tokenProvider = getToken
            }
        }
    }
