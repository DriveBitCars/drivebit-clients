package my.drivebit.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClientEngine(): HttpClientEngine = Darwin.create()

actual fun getBaseUrl(): String = DEFAULT_BASE_URL
