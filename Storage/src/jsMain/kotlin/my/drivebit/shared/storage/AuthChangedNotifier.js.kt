package my.drivebit.shared.storage

import kotlinx.browser.window
import org.w3c.dom.events.Event

internal actual fun dispatchAuthChangedEvent() {
    window.dispatchEvent(Event("drivebit-auth-changed"))
}
