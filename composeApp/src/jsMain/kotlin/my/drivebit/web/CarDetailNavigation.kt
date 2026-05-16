package my.drivebit.web

import kotlinx.browser.window
import my.drivebit.navigation.NavigationController
import my.drivebit.utils.END_AT
import my.drivebit.utils.START_AT
import my.drivebit.utils.dateToEndAtIso
import my.drivebit.utils.dateToStartAtIso
import my.drivebit.utils.encodeUrlParameter
fun navigateToCarDetail(
    carId: String,
    startDate: String?,
    endDate: String?,
    navigationController: NavigationController?,
) {
    val params = mutableListOf("id=${carId.encodeUrlParameter()}")
    dateToStartAtIso(startDate)?.let {
        params.add("$START_AT=${it.encodeUrlParameter()}")
    }
    dateToEndAtIso(endDate)?.let {
        params.add("$END_AT=${it.encodeUrlParameter()}")
    }
    val url = "/car-detail?${params.joinToString("&")}"
    if (navigationController != null) {
        navigationController.navigateTo(url)
    } else {
        window.location.href = url
    }
}
