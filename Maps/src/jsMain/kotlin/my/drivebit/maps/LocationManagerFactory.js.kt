package my.drivebit.maps

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun createLocationManager(): LocationManager = LocationManagerJs()

class LocationManagerJs : LocationManager {
    override suspend fun getCurrentLocation(): my.drivebit.maps.models.Location? {
        return suspendCancellableCoroutine { continuation ->
            if (!js("'geolocation' in navigator").unsafeCast<Boolean>()) {
                console.warn("Geolocation API is not supported in this browser")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val options = js("{}").unsafeCast<dynamic>()
            options.enableHighAccuracy = true
            options.timeout = 15000
            options.maximumAge = 0

            console.log("Requesting geolocation permission...")

            navigator.geolocation.getCurrentPosition(
                { position ->
                    console.log("Geolocation success: ${position.coords.latitude}, ${position.coords.longitude}")
                    val location =
                        my.drivebit.maps.models.Location(
                            latitude = position.coords.latitude,
                            longitude = position.coords.longitude,
                        )
                    continuation.resume(location)
                },
                { error ->
                    when (error.code) {
                        1 -> console.warn("Geolocation permission denied by user")
                        2 -> console.warn("Geolocation position unavailable: ${error.message}")
                        3 -> console.warn("Geolocation request timeout: ${error.message}")
                        else -> console.warn("Geolocation error (code ${error.code}): ${error.message}")
                    }
                    continuation.resume(null)
                },
                options,
            )
        }
    }

    override fun hasPermission(): Boolean = js("'geolocation' in navigator").unsafeCast<Boolean>()

    override suspend fun requestPermission(): Boolean = getCurrentLocation() != null
}

external val navigator: Navigator

external interface Navigator {
    val geolocation: Geolocation
}

external interface Geolocation {
    fun getCurrentPosition(
        success: (Position) -> Unit,
        error: (GeolocationPositionError) -> Unit,
        options: dynamic = definedExternally,
    )
}

external interface Position {
    val coords: Coordinates
}

external interface Coordinates {
    val latitude: Double
    val longitude: Double
    val accuracy: Double?
}

external interface GeolocationPositionError {
    val code: Int
    val message: String
}

object GeolocationErrorCodes {
    const val PERMISSION_DENIED = 1
    const val POSITION_UNAVAILABLE = 2
    const val TIMEOUT = 3
}
