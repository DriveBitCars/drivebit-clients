package my.drivebit.web

import my.drivebit.navigation.SeoLandingBlock
import my.drivebit.navigation.SeoLandingBlocks

fun resolveSearchPageHeadline(
    path: String,
    brandName: String?,
    block: SeoLandingBlock? = SeoLandingBlocks.blockForPath(path),
): String {
    if (block != null) {
        return block.h1?.takeIf { it.isNotBlank() } ?: block.h2
    }
    val brand = brandName?.takeIf { it.isNotBlank() }
    if (brand != null) {
        return "Аренда $brand в Москве без водителя"
    }
    return "Поиск автомобилей для аренды в Москве"
}
