package my.drivebit.utils

fun locationPathForSeo(href: String): String =
    href
        .substringBefore('?')
        .substringBefore('#')
        .removeSuffix("/")
        .ifEmpty { "/" }

fun commitSearchLocationSeo(
    locationHref: String,
    updateMetaForPath: (String) -> Unit,
) {
    updateMetaForPath(locationPathForSeo(locationHref))
}
