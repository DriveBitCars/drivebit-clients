package my.drivebit.navigation

fun isChatBundlePath(pathname: String): Boolean {
    val path = pathname.removeSuffix("/").ifEmpty { "/" }
    return path == "/chats" ||
        path.startsWith("/chats/") ||
        path == "/chat" ||
        path.startsWith("/chat/")
}

val chatBundleShellRoutes: List<String> =
    listOf(
        "chats",
        "chat",
    )
