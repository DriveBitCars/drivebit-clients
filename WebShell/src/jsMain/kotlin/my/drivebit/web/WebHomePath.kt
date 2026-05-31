package my.drivebit.web

import kotlinx.browser.window
import my.drivebit.repositories.MyCityStorageKeys
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.cityNameToSlug

private const val DEFAULT_CITY_NAME = "Москва"

/** Path to the city home URL for full page redirects (e.g. after logout). */
fun homePathHref(storage: Storage): String {
    parseCitySlugFromPath(window.location.pathname)?.let { return "/$it" }
    val name = storage.getString(MyCityStorageKeys.NAME_KEY)
    return "/${cityNameToSlug(name.ifBlank { DEFAULT_CITY_NAME })}"
}
