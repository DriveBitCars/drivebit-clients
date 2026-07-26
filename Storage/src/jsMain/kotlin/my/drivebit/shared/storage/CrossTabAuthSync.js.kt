package my.drivebit.shared.storage

import kotlinx.browser.window

private var installed = false

internal fun installCrossTabAuthSyncIfNeeded() {
    if (installed) {
        return
    }
    installed = true

    window.addEventListener(
        "storage",
        { event ->
            val key = event.asDynamic().key as? String
            if (key == "auth_token" || key == "refresh_token") {
                dispatchAuthChangedEvent()
            }
        },
    )
}
