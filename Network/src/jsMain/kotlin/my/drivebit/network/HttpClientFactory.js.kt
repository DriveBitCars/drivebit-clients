package my.drivebit.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import kotlinx.browser.window

actual fun createPlatformHttpClientEngine(): HttpClientEngine = Js.create()

actual fun getBaseUrl(): String {
    val protocol = window.location.protocol
    val hostname = window.location.hostname

    // Для dev.drivebit.my и drivebit.my используем прокси через тот же домен
    // nginx проксирует /api/ -> http://api.drivebit.my:5000/
    return if (hostname == "dev.drivebit.my" || hostname == "drivebit.my") {
        "$protocol//$hostname/api/"
    } else {
        DEFAULT_BASE_URL
    }
}
