package my.drivebit.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClientEngine(): HttpClientEngine

fun createHttpClientWithConfig(
    json: Json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        },
): HttpClient =
    HttpClient(createPlatformHttpClientEngine()) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
