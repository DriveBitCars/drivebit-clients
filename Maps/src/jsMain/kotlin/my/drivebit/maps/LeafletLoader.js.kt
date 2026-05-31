package my.drivebit.maps

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend fun awaitLeafletReady() {
    if (js("typeof L !== 'undefined'").unsafeCast<Boolean>()) {
        return
    }
    suspendCoroutine { cont ->
        fun tryLoad() {
            if (js("typeof L !== 'undefined'").unsafeCast<Boolean>()) {
                cont.resume(Unit)
                return
            }
            val loader = js("window.drivebitLoadLeaflet").unsafeCast<(() -> dynamic)?>()
            if (loader != null) {
                loader().then { cont.resume(Unit) }
            } else {
                window.setTimeout({ tryLoad() }, 50)
            }
        }
        tryLoad()
    }
}
