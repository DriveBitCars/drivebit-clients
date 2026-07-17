package my.drivebit.web

import my.drivebit.navigation.SeoLandingBlock
import my.drivebit.navigation.SeoLandingBlocks
import my.drivebit.utils.cityInPrepositional
import my.drivebit.utils.parseCitySlugFromSearchPath

fun resolveSearchPageHeadline(
    path: String,
    brandName: String?,
    cityName: String? = null,
    block: SeoLandingBlock? = SeoLandingBlocks.blockForPath(path),
): String {
    if (block != null) {
        return block.h1?.takeIf { it.isNotBlank() } ?: block.h2
    }
    val brand = brandName?.takeIf { it.isNotBlank() }
    val cityLabel =
        cityName?.takeIf { it.isNotBlank() }?.let { cityInPrepositional(it) }
            ?: "Москве"
    if (brand != null) {
        return "Аренда $brand в $cityLabel без водителя"
    }
    if (parseCitySlugFromSearchPath(path) != null || cityName != null) {
        return "Поиск автомобилей для аренды в $cityLabel"
    }
    return "Поиск автомобилей для аренды в Москве"
}
