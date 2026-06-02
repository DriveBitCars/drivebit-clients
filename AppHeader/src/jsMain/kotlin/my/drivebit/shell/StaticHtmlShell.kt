package my.drivebit.shell

private val STATIC_HTML_SHELL_PATHS =
    setOf(
        "/contacts",
        "/cookies",
        "/privacy",
        "/offer",
        "/payment-success",
        "/payment-failure",
    )

fun isStaticHtmlShellPath(pathname: String): Boolean {
    val normalized = pathname.removeSuffix("/").ifEmpty { "/" }
    return normalized in STATIC_HTML_SHELL_PATHS
}
