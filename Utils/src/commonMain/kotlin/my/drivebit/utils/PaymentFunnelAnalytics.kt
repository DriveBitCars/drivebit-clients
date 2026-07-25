package my.drivebit.utils

object PaymentFunnelGoals {
    const val CLICK = "pay_click"
    const val REDIRECT = "pay_redirect"
    const val FAIL = "pay_fail"
    const val ALREADY_PAID = "pay_already_paid"
    const val SUCCESS_PAGE = "payment_success"
    const val FAILURE_PAGE = "payment_failure"
}

enum class PaymentFunnelSource(
    val id: String,
) {
    Chat("chat"),
    MyBookings("my_bookings"),
    PaymentLink("payment_link"),
    CarDetail("car_detail"),
}

enum class PaymentFunnelKind(
    val id: String,
) {
    Full("full"),
    Prepay("prepay"),
}

private const val MESSAGE_MAX_LEN = 120

fun paymentFunnelParams(
    source: PaymentFunnelSource,
    kind: PaymentFunnelKind,
    bookingId: String,
    message: String? = null,
): Map<String, String> {
    val params =
        linkedMapOf(
            "source" to source.id,
            "kind" to kind.id,
            "bookingId" to bookingId,
        )
    if (!message.isNullOrBlank()) {
        params["message"] = message.take(MESSAGE_MAX_LEN)
    }
    return params
}
