package my.drivebit.navigation

enum class WebBundle {
    Main,
    Profile,
    Account,
    Auth,
    Chat,
    OwnerCar,
    CarDetail,
}

fun pathWithoutQuery(path: String): String =
    path
        .substringBefore('?')
        .substringBefore('#')
        .removeSuffix("/")
        .ifEmpty { "/" }

fun webBundleForPath(pathname: String): WebBundle {
    val path = pathWithoutQuery(pathname)
    return when {
        isProfileBundlePath(path) -> WebBundle.Profile
        isAccountBundlePath(path) -> WebBundle.Account
        isAuthBundlePath(path) -> WebBundle.Auth
        isChatBundlePath(path) -> WebBundle.Chat
        isOwnerCarBundlePath(path) -> WebBundle.OwnerCar
        path.startsWith("/car-detail") || path.startsWith("/car-photos-gallery") -> WebBundle.CarDetail
        else -> WebBundle.Main
    }
}

fun requiresFullPageNavigation(
    currentPathname: String,
    targetPath: String,
): Boolean {
    if (webBundleForPath(currentPathname) != webBundleForPath(targetPath)) return true
    val current = pathWithoutQuery(currentPathname)
    val target = pathWithoutQuery(targetPath)
    return my.drivebit.web.isSearchPath(current) != my.drivebit.web.isSearchPath(target)
}
