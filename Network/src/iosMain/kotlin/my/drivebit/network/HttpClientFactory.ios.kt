package my.drivebit.network

import io.ktor.client.darwin.Darwin
import io.ktor.client.engine.HttpClientEngine

actual fun createPlatformHttpClientEngine(): HttpClientEngine = Darwin.create()
