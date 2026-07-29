package my.drivebit.utils

object BookingFunnelGoals {
    const val LEGACY = "bron"
    const val CLICK = "bron_click"
    const val AUTO = "bron_auto"
    const val CREATE_OK = "bron_create_ok"
    const val CREATE_FAIL = "bron_create_fail"
}

enum class BookingFunnelSource(
    val id: String,
) {
    Click("click"),
    Auto("auto"),
}

private const val BOOKING_FUNNEL_MESSAGE_MAX_LEN = 120

fun bookingFunnelIntentParams(
    source: BookingFunnelSource,
    carId: String,
): Map<String, String> =
    linkedMapOf(
        "source" to source.id,
        "carId" to carId,
    )

fun bookingFunnelCreateParams(
    source: BookingFunnelSource,
    carId: String,
    bookingId: String? = null,
    message: String? = null,
): Map<String, String> {
    val params =
        linkedMapOf(
            "source" to source.id,
            "carId" to carId,
        )
    if (!bookingId.isNullOrBlank()) {
        params["bookingId"] = bookingId
    }
    if (!message.isNullOrBlank()) {
        params["message"] = message.take(BOOKING_FUNNEL_MESSAGE_MAX_LEN)
    }
    return params
}
