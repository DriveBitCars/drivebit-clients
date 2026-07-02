package my.drivebit.navigation

import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.repositories.MyCityStorageKeys
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.pageDescription
import my.drivebit.utils.pageTitle
import my.drivebit.utils.resolveCityNameForMeta
import my.drivebit.web.filterTitleFromPathSegment
import my.drivebit.web.parseCityPath

object MetaTags {
    private const val HOME_TITLE = "DriveBit - Аренда автомобилей от собственников | Дешевле проката на 40%"
    private const val HOME_DESCRIPTION =
        "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7."

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
                    title = HOME_TITLE,
                    description = HOME_DESCRIPTION,
                    path = "/",
                ),
            "/contacts" to
                PageMeta(
                    title = "Контакты - DriveBit",
                    description = "Контактные данные и реквизиты DriveBit. Аренда автомобилей от собственников.",
                    path = "/contacts",
                ),
            "/offer" to
                PageMeta(
                    title = "Оферта - DriveBit",
                    description = "Публичная оферта сервиса аренды автомобилей DriveBit. Условия аренды и сдачи авто.",
                    path = "/offer",
                ),
            "/privacy" to
                PageMeta(
                    title = "Политика конфиденциальности - DriveBit",
                    description = "Политика обработки персональных данных сервиса аренды автомобилей DriveBit.",
                    path = "/privacy",
                ),
            "/cookies" to
                PageMeta(
                    title = "Политика cookies - DriveBit",
                    description = "Информация об использовании файлов cookies на сайте DriveBit.",
                    path = "/cookies",
                ),
            "/moskva" to
                PageMeta(
                    title = "Аренда автомобилей в Москве - DriveBit",
                    description = "Аренда авто в Москве у собственников. Дешевле проката, полная страховка и поддержка 24/7.",
                    path = "/moskva",
                ),
            "/moskva/k-rodnym" to
                PageMeta(
                    title = "Аренда авто для поездки к родным в Москве - DriveBit",
                    description = "Подберите автомобиль для поездки к родным в Москве. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/k-rodnym",
                ),
            "/moskva/komandirovki" to
                PageMeta(
                    title = "Аренда авто для командировок в Москве - DriveBit",
                    description = "Подберите автомобиль для командировки в Москве. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/komandirovki",
                ),
            "/moskva/kanikuly" to
                PageMeta(
                    title = "Аренда авто на каникулы в Москве - DriveBit",
                    description = "Подберите автомобиль на каникулы в Москве. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/kanikuly",
                ),
            "/moskva/arenda-vnedorozhnika-bez-voditelya" to
                PageMeta(
                    title = "Аренда внедорожника без водителя в Москве через сервис DriveBit",
                    description = "Аренда внедорожника без водителя в Москве через сервис DriveBit. Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!",
                    path = "/moskva/arenda-vnedorozhnika-bez-voditelya",
                ),
            "/moskva/pereezd" to
                PageMeta(
                    title = "Аренда авто для переезда в Москве - DriveBit",
                    description = "Подберите автомобиль для переезда в Москве. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/pereezd",
                ),
            "/moskva/poblizosti" to
                PageMeta(
                    title = "Аренда авто поблизости в Москве - DriveBit",
                    description = "Найдите автомобили поблизости в Москве. Быстрая аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/poblizosti",
                ),
            "/moskva/puteshestviya" to
                PageMeta(
                    title = "Аренда авто для путешествий в Москве - DriveBit",
                    description = "Подберите автомобиль для путешествий из Москвы. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/puteshestviya",
                ),
            "/moskva/za-gorod" to
                PageMeta(
                    title = "Аренда авто за город в Москве - DriveBit",
                    description = "Подберите автомобиль для поездки за город из Москвы. Аренда у собственников, прозрачные условия и поддержка 24/7.",
                    path = "/moskva/za-gorod",
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
            "/payment" to
                PageMeta(
                    title = "Оплата бронирования - DriveBit",
                    description = "Оплата бронирования автомобиля в DriveBit.",
                    path = "/payment",
                    noindex = true,
                ),
            "/download-booking-contract" to
                PageMeta(
                    title = "Договор аренды - DriveBit",
                    description = "Скачивание договора аренды в DriveBit.",
                    path = "/download-booking-contract",
                    noindex = true,
                ),
            "/payment-success" to
                PageMeta(
                    title = "Оплата успешна - DriveBit",
                    description = "Платёж в DriveBit успешно завершён.",
                    path = "/payment-success",
                    noindex = true,
                ),
            "/payment-failure" to
                PageMeta(
                    title = "Оплата не прошла - DriveBit",
                    description = "Платёж в DriveBit не был завершён.",
                    path = "/payment-failure",
                    noindex = true,
                ),
            "/search" to
                PageMeta(
                    title = "Поиск автомобилей - DriveBit",
                    description = "Расширенный поиск аренды автомобилей в Москве по марке, цене, типу кузова и приводу.",
                    path = "/search",
                ),
            "/car-detail" to
                PageMeta(
                    title = "Карточка автомобиля - DriveBit",
                    description = "Подробная информация об автомобиле для аренды на DriveBit.",
                    path = "/car-detail",
                    noindex = true,
                ),
            "/car-photos-gallery" to
                PageMeta(
                    title = "Фотографии автомобиля - DriveBit",
                    description = "Фотографии автомобиля для аренды на DriveBit.",
                    path = "/car-photos-gallery",
                    noindex = true,
                ),
        )

    fun updateForPath(
        path: String,
        storage: Storage? = null,
    ) {
        val normalizedPath = normalizePath(path)
        val lookupPath =
            when {
                normalizedPath.startsWith("/car-detail") -> "/car-detail"
                normalizedPath.startsWith("/car-photos-gallery") -> "/car-photos-gallery"
                else -> normalizedPath
            }
        val seoBlock = SeoLandingBlocks.blockForPath(lookupPath)
        val pageMeta =
            cityPageMeta(lookupPath, storage, seoBlock)
                ?: pages[lookupPath]
                ?: if (seoBlock?.title != null) {
                    PageMeta(
                        title = seoBlock.title,
                        description = seoBlock.description ?: HOME_DESCRIPTION,
                        path = lookupPath,
                    )
                } else {
                    PageMeta(
                        title = HOME_TITLE,
                        description = HOME_DESCRIPTION,
                        path = lookupPath,
                    )
                }

        val canonicalPath = seoCanonicalPath(lookupPath, pageMeta.path)

        document.title = pageMeta.title

        updateMetaTag("description", pageMeta.description)
        updateMetaTag("og:title", pageMeta.title, property = true)
        updateMetaTag("og:description", pageMeta.description, property = true)
        updateMetaTag("og:url", getFullUrl(canonicalPath, includeQuery = true), property = true)
        updateMetaTag("twitter:title", pageMeta.title)
        updateMetaTag("twitter:description", pageMeta.description)
        updateMetaTag("twitter:url", getFullUrl(canonicalPath, includeQuery = true))

        if (pageMeta.noindex) {
            updateMetaTag("robots", "noindex, nofollow")
            removeCanonicalUrl()
        } else {
            removeMetaTag("robots")
            updateCanonicalUrl(getFullUrl(canonicalPath, includeQuery = false))
        }

        SeoBlocks.updateForPath(lookupPath)
    }

    private fun seoCanonicalPath(
        lookupPath: String,
        pagePath: String,
    ): String =
        if (lookupPath == "/" || lookupPath.isEmpty()) {
            "/moskva"
        } else {
            pagePath
        }

    private fun cityPageMeta(
        lookupPath: String,
        storage: Storage?,
        seoBlock: SeoLandingBlock?,
    ): PageMeta? {
        val cityPath = parseCityPath(lookupPath) ?: return null
        val storedCityName =
            storage?.getString(MyCityStorageKeys.NAME_KEY, "").orEmpty()
        val cityName = resolveCityNameForMeta(cityPath.citySlug, storedCityName)
        val filterTitle = filterTitleFromPathSegment(cityPath.filterSlug)
        val title = pageTitle(cityPath.citySlug, cityPath.filterSlug, cityName, filterTitle)
        val description =
            pageDescription(cityPath.citySlug, cityPath.filterSlug, cityName, filterTitle)
        return PageMeta(
            title = title,
            description = description,
            path = lookupPath,
        )
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

    private fun removeCanonicalUrl() {
        val canonical = document.querySelector("link[rel='canonical']") as? org.w3c.dom.HTMLLinkElement
        canonical?.let { document.head?.removeChild(it) }
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

    private fun normalizePath(path: String): String {
        val withoutQuery = path.substringBefore('?').substringBefore('#')
        if (withoutQuery.isBlank()) return "/"
        val normalized = if (withoutQuery.startsWith("/")) withoutQuery else "/$withoutQuery"
        return normalized.trimEnd('/').ifEmpty { "/" }
    }
}
