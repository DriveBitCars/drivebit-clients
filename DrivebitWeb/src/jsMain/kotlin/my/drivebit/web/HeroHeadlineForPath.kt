package my.drivebit.web

import my.drivebit.utils.cityNameFromSlug
import my.drivebit.utils.pageHeadline

fun heroHeadlineForCityPath(path: String): String? {
    val cityPath = parseCityPath(path) ?: return null
    val cityName = cityNameFromSlug(cityPath.citySlug) ?: return null
    val filterTitle = filterTitleFromPathSegment(cityPath.filterSlug)
    return pageHeadline(
        citySlug = cityPath.citySlug,
        filterSlug = cityPath.filterSlug,
        cityName = cityName,
        filterTitle = filterTitle,
    )
}

fun activeFilterTitleForCityPath(path: String): String? {
    val cityPath = parseCityPath(path) ?: return null
    return filterTitleFromPathSegment(cityPath.filterSlug)
}
