package my.drivebit.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createPlatformHttpClientEngine(): HttpClientEngine = Js.create()

actual fun getBaseUrl(): String = DEFAULT_BASE_URL
