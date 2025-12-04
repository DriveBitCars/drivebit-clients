package my.drivebit.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import kotlinx.browser.window

actual fun createPlatformHttpClientEngine(): HttpClientEngine = Js.create()

actual fun getBaseUrl(): String {
    val hostname = window.location.hostname
    return "https://$hostname/api/"
}
