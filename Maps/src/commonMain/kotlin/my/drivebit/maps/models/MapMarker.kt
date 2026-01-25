package my.drivebit.maps.models

data class MapMarker(
    val id: String,
    val location: Location,
    val title: String,
    val snippet: String? = null,
)
