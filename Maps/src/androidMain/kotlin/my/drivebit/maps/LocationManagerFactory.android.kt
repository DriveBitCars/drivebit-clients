package my.drivebit.maps

import my.drivebit.maps.models.Location

actual fun createLocationManager(): LocationManager = LocationManagerAndroid()

class LocationManagerAndroid : LocationManager {
    override suspend fun getCurrentLocation(): Location? = null

    override fun hasPermission(): Boolean = false

    override suspend fun requestPermission(): Boolean = false
}
