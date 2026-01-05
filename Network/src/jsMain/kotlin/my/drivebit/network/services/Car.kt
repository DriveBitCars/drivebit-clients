package my.drivebit.network.services

actual fun getCurrentHostnameImplForCar(): String {
    return kotlinx.browser.window.location.hostname
}
