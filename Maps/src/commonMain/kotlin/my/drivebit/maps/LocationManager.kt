package my.drivebit.maps

import my.drivebit.maps.models.Location

interface LocationManager {
    suspend fun getCurrentLocation(): Location?

    fun hasPermission(): Boolean

    suspend fun requestPermission(): Boolean
}
