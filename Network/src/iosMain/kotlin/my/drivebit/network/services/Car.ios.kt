package my.drivebit.network.services

internal actual fun getCurrentDomainForCar(): String {
    // For iOS, use drivebit.ru as default (can be configured if needed)
    return "drivebit.ru"
}
