package my.drivebit.web

import kotlinx.browser.window
import my.drivebit.utils.END_AT
import my.drivebit.utils.START_AT
import my.drivebit.utils.dateToStartAtIso
import my.drivebit.utils.dateToEndAtIso
import my.drivebit.utils.encodeUrlParameter

fun buildCarDetailUrl(
    carId: String,
    startDate: String? = null,
    endDate: String? = null,
): String {
    val params = mutableListOf("id=${carId.encodeUrlParameter()}")
    dateToStartAtIso(startDate)?.let {
        params.add("$START_AT=${it.encodeUrlParameter()}")
    }
    dateToEndAtIso(endDate)?.let {
        params.add("$END_AT=${it.encodeUrlParameter()}")
    }
    return "/car-detail?${params.joinToString("&")}"
}

fun navigateToCarDetail(
    carId: String,
    startDate: String?,
    endDate: String?,
) {
    window.location.href = buildCarDetailUrl(carId, startDate, endDate)
}
