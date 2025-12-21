package my.drivebit.maps.models

data class Location(
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        fun kaliningrad() = Location(latitude = 54.7104, longitude = 20.4522)

        fun moscow() = Location(latitude = 55.7558, longitude = 37.6173)

        fun saintPetersburg() = Location(latitude = 59.9343, longitude = 30.3351)
    }
}
