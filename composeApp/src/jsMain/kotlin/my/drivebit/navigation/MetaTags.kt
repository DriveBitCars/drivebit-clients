package my.drivebit.navigation

import kotlinx.browser.document
import kotlinx.browser.window

object MetaTags {
    private const val DEFAULT_TITLE = "DriveBit - Аренда автомобилей"
    private const val DEFAULT_DESCRIPTION = "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7."

    data class PageMeta(
        val title: String,
        val description: String,
        val path: String,
        val noindex: Boolean = false,
    )

    private val pages =
        mapOf(
            "/" to
                PageMeta(
                    title = "DriveBit - Аренда автомобилей",
                    description = "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7.",
                    path = "/",
                ),
            "/list-your-car" to
                PageMeta(
                    title = "Сдать авто в аренду - DriveBit",
                    description = "Сдавайте свой автомобиль в аренду и зарабатывайте до 40% больше, чем в прокате. Полная страховка, поддержка 24/7.",
                    path = "/list-your-car",
                ),
            "/login-by-phone" to
                PageMeta(
                    title = "Вход по телефону - DriveBit",
                    description = "Войдите в свой аккаунт DriveBit по номеру телефона для аренды автомобилей от собственников.",
                    path = "/login-by-phone",
                    noindex = true,
                ),
            "/login-by-mail" to
                PageMeta(
                    title = "Вход по email - DriveBit",
                    description = "Войдите в свой аккаунт DriveBit по email для аренды автомобилей от собственников.",
                    path = "/login-by-mail",
                    noindex = true,
                ),
            "/signup" to
                PageMeta(
                    title = "Регистрация - DriveBit",
                    description = "Создайте аккаунт в DriveBit и начните арендовать автомобили от собственников.",
                    path = "/signup",
                    noindex = true,
                ),
            "/chats" to
                PageMeta(
                    title = "Входящие - DriveBit",
                    description = "Сообщения и чаты в DriveBit. Общайтесь с арендаторами и владельцами автомобилей.",
                    path = "/chats",
                    noindex = true,
                ),
            "/chat" to
                PageMeta(
                    title = "Чат - DriveBit",
                    description = "Переписка в DriveBit.",
                    path = "/chat",
                    noindex = true,
                ),
        )

    fun updateForPath(path: String) {
        val pageMeta = pages[path] ?: pages["/"] ?: return

        document.title = pageMeta.title

        updateMetaTag("description", pageMeta.description)
        updateMetaTag("og:title", pageMeta.title, property = true)
        updateMetaTag("og:description", pageMeta.description, property = true)
        updateMetaTag("og:url", getFullUrl(path, includeQuery = true), property = true)

        updateCanonicalUrl(getFullUrl(path, includeQuery = false))

        if (pageMeta.noindex) {
            updateMetaTag("robots", "noindex, nofollow")
        } else {
            removeMetaTag("robots")
        }
    }

    private fun updateMetaTag(
        name: String,
        content: String,
        property: Boolean = false,
    ) {
        val selector = if (property) "meta[property='$name']" else "meta[name='$name']"
        var meta = document.querySelector(selector) as? org.w3c.dom.HTMLMetaElement

        if (meta == null) {
            meta = document.createElement("meta") as org.w3c.dom.HTMLMetaElement
            if (property) {
                meta.setAttribute("property", name)
            } else {
                meta.setAttribute("name", name)
            }
            document.head?.appendChild(meta)
        }

        meta.content = content
    }

    private fun removeMetaTag(name: String) {
        val selector = "meta[name='$name']"
        val meta = document.querySelector(selector) as? org.w3c.dom.HTMLMetaElement
        meta?.let { document.head?.removeChild(it) }
    }

    private fun updateCanonicalUrl(url: String) {
        var canonical = document.querySelector("link[rel='canonical']") as? org.w3c.dom.HTMLLinkElement

        if (canonical == null) {
            canonical = document.createElement("link") as org.w3c.dom.HTMLLinkElement
            canonical.setAttribute("rel", "canonical")
            document.head?.appendChild(canonical)
        }

        canonical.href = url
    }

    private fun getFullUrl(
        path: String,
        includeQuery: Boolean = false,
    ): String {
        val cleanPath = path.trimEnd('/').ifEmpty { "/" }
        val query = if (includeQuery) window.location.search else ""
        return "${window.location.protocol}//${window.location.host}$cleanPath$query"
    }
}
