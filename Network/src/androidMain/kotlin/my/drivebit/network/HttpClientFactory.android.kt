package my.drivebit.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun createPlatformHttpClientEngine(): HttpClientEngine =
    Android.create {
        connectTimeout = 10_000
        socketTimeout = 10_000
    }
