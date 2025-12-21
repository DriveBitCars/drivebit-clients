package my.drivebit.maps.models

data class MapCameraPosition(
    val location: Location,
    val zoom: Float = 15f,
) {
    companion object {
        fun default() = MapCameraPosition(location = Location.moscow(), zoom = 13f)
    }
}
