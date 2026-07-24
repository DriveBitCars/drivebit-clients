package my.drivebit.search

import kotlinx.browser.window
import my.drivebit.utils.SearchUrlParts
import my.drivebit.utils.buildSearchUrl
import my.drivebit.utils.cityNameToSlug

fun SearchFilterSet.toSearchUrlParts(): SearchUrlParts {
    val resolvedBrandSlug =
        brandSlug?.takeIf { it.isNotEmpty() }
            ?: brandName?.takeIf { it.isNotEmpty() }?.let { cityNameToSlug(it) }
    val resolvedModelSlug =
        modelSlug?.takeIf { it.isNotEmpty() }
            ?: modelName?.takeIf { it.isNotEmpty() }?.let { cityNameToSlug(it) }
    return SearchUrlParts(
        citySlug = citySlug ?: if (resolvedBrandSlug == null) "moskva" else null,
        brandSlug = resolvedBrandSlug,
        modelSlug = resolvedModelSlug,
        startDate = startDate,
        endDate = endDate,
        dailyRateMin = dailyRateMin,
        dailyRateMax = dailyRateMax,
        driveType = driveType,
        driveTypeLabel = driveTypeLabel,
        bodyType = bodyType,
        bodyTypeLabel = bodyTypeLabel,
        seatsMin = seatsMin,
        yearMin = yearMin,
        yearMax = yearMax,
        mileageMin = mileageMin,
        page = page,
    )
}

fun navigateSearchFilters(filters: SearchFilterSet) {
    window.history.pushState(null, "", buildSearchUrl(filters.toSearchUrlParts()))
}

fun resetSearchFilters(
    filters: SearchFilterSet,
    selectedCitySlug: String?,
): SearchFilterSet = SearchFilterSet(citySlug = filters.citySlug ?: selectedCitySlug ?: "moskva")

fun currentLocationHref(): String = window.location.pathname + window.location.search
