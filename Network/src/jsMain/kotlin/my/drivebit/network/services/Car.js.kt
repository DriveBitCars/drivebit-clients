package my.drivebit.network.services

import kotlinx.browser.window

internal actual fun getCurrentDomainForCar(): String {
    val hostname = window.location.hostname
    // Return hostname as-is (drivebit.ru, drivebit.my, etc.)
    return hostname
}
