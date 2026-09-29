package my.drivebit.utils

const val GITHUB_PAGES_BASE_PATH = "/drivebit-clients"

fun isGithubPagesHost(hostname: String): Boolean =
    hostname.equals("drivebitcars.github.io", ignoreCase = true)

fun stripWebBasePath(
    pathname: String,
    hostname: String,
): String {
    if (!isGithubPagesHost(hostname)) return pathname
    val base = GITHUB_PAGES_BASE_PATH
    return when {
        pathname == base || pathname == "$base/" -> "/"
        pathname.startsWith("$base/") -> pathname.removePrefix(base)
        else -> pathname
    }
}

fun withWebBasePath(
    appPath: String,
    hostname: String,
): String {
    if (!isGithubPagesHost(hostname)) return appPath
    if (appPath.startsWith("http://") || appPath.startsWith("https://")) return appPath
    if (appPath.startsWith(GITHUB_PAGES_BASE_PATH)) return appPath
    val normalized = if (appPath.startsWith("/")) appPath else "/$appPath"
    return GITHUB_PAGES_BASE_PATH + normalized
}
