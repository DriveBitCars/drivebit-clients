package my.drivebit.web.analytics

import kotlinx.browser.window
import kotlin.js.JsModule
import kotlin.js.JsNonModule
import kotlin.js.js

private const val HAWK_INTEGRATION_TOKEN =
    "eyJpbnRlZ3JhdGlvbklkIjoiNjU4Mzk0NjQtN2Q2Yy00MzIxLWIwY2EtNDMwOWE5ODEzYzEwIiwic2VjcmV0IjoiN2ZlZDdhMjYtMDJhNS00MmMwLWFhNGYtNTkwY2Q1Zjg0OTJkIn0="

@JsModule("@hawk.so/javascript")
@JsNonModule
private external class HawkCatcher(settings: dynamic)

private var hawkCatcher: HawkCatcher? = null

internal fun hawkEnvironment(hostname: String): String? =
    when (hostname.lowercase()) {
        "drivebit.ru", "www.drivebit.ru" -> "production"
        "dev.drivebit.ru", "dev.drivebit.my", "drivebitcars.github.io" -> "development"
        else -> null
    }

internal fun shouldSendHawkEvent(title: String?): Boolean {
    val normalized = title?.trim()?.lowercase().orEmpty()
    if (normalized.isEmpty()) return true
    return normalized != "script error" && normalized != "script error."
}

fun initializeHawkErrorTracking() {
    if (hawkCatcher != null) return

    val environment = hawkEnvironment(window.location.hostname) ?: return
    val settings = js("{}")
    settings.token = HAWK_INTEGRATION_TOKEN
    settings.consoleTracking = false
    settings.breadcrumbs = false
    settings.context = js("{}")
    settings.context.environment = environment
    settings.beforeSend = { event: dynamic ->
        val title = event.title as? String
        if (shouldSendHawkEvent(title)) event else false
    }

    hawkCatcher = HawkCatcher(settings)
    window.asDynamic().__drivebitHawk = hawkCatcher
}
