package my.drivebit.maps.models

data class MapMarker(
    val id: String,
    val location: Location,
    val title: String? = null,
    val snippet: String? = null,
)
