package my.drivebit.utils

object ContractFunnelGoals {
    const val SIGN_CLICK = "contract_sign_click"
    const val SIGN_OK = "contract_sign_ok"
    const val SIGN_FAIL = "contract_sign_fail"
    const val DOWNLOAD_CLICK = "contract_download_click"
    const val DOWNLOAD_READY = "contract_download_ready"
    const val DOWNLOAD_INCOMPLETE = "contract_download_incomplete"
    const val DOWNLOAD_FAIL = "contract_download_fail"
}

enum class ContractFunnelSource(
    val id: String,
) {
    Chat("chat"),
    MyBookings("my_bookings"),
    MyDeals("my_deals"),
    DownloadPage("download_page"),
}

enum class ContractFunnelRole(
    val id: String,
) {
    Owner("owner"),
    Renter("renter"),
}

private const val MESSAGE_MAX_LEN = 120

fun contractFunnelParams(
    source: ContractFunnelSource,
    role: ContractFunnelRole?,
    bookingId: String,
    message: String? = null,
): Map<String, String> {
    val params =
        linkedMapOf(
            "source" to source.id,
            "bookingId" to bookingId,
        )
    if (role != null) {
        params["role"] = role.id
    }
    if (!message.isNullOrBlank()) {
        params["message"] = message.take(MESSAGE_MAX_LEN)
    }
    return params
}
