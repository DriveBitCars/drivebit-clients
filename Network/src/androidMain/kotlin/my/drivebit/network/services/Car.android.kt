package my.drivebit.network.services

internal actual fun getCurrentDomainForCar(): String {
    // For Android, use drivebit.ru as default (can be configured via BuildConfig if needed)
    return "drivebit.ru"
}
