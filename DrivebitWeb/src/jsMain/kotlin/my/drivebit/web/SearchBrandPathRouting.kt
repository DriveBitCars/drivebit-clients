package my.drivebit.web

import my.drivebit.utils.canonicalizeBrandSearchPath
import my.drivebit.utils.cityNameToSlug
import my.drivebit.utils.parseBrandSlugFromPath
import my.drivebit.utils.pathForBrandSlug

fun parseSearchBrandSlugFromPath(pathname: String): String? = parseBrandSlugFromPath(pathname)

fun searchPathForBrandName(brandName: String): String = pathForBrandSlug(cityNameToSlug(brandName))

fun isSearchBrandPath(pathname: String): Boolean = parseSearchBrandSlugFromPath(pathname) != null

fun canonicalSearchBrandPath(pathname: String): String? = canonicalizeBrandSearchPath(pathname)
