package my.drivebit.utils

private const val DEFAULT_CITY_NAME = "Москва"

private val FILTERS_WITH_GENITIVE_FROM_CITY =
    setOf(
        "Путешествия",
        "За город",
    )

private val CITY_FORMS: Map<String, Pair<String, String>> =
    mapOf(
        "Москва" to ("Москве" to "Москвы"),
        "Санкт-Петербург" to ("Санкт-Петербурге" to "Санкт-Петербурга"),
        "Санкт Петербург" to ("Санкт-Петербурге" to "Санкт-Петербурга"),
        "Казань" to ("Казани" to "Казани"),
        "Нижний Новгород" to ("Нижнем Новгороде" to "Нижнего Новгорода"),
        "Екатеринбург" to ("Екатеринбурге" to "Екатеринбурга"),
        "Новосибирск" to ("Новосибирске" to "Новосибирска"),
        "Краснодар" to ("Краснодаре" to "Краснодара"),
        "Сочи" to ("Сочи" to "Сочи"),
        "Ростов-на-Дону" to ("Ростове-на-Дону" to "Ростова-на-Дону"),
        "Владивосток" to ("Владивостоке" to "Владивостока"),
        "Калининград" to ("Калининграде" to "Калининграда"),
        "Йошкар-Ола" to ("Йошкар-Оле" to "Йошкар-Олы"),
        "Уфа" to ("Уфе" to "Уфы"),
        "Самара" to ("Самаре" to "Самары"),
        "Челябинск" to ("Челябинске" to "Челябинска"),
        "Омск" to ("Омске" to "Омска"),
        "Красноярск" to ("Красноярске" to "Красноярска"),
        "Воронеж" to ("Воронеже" to "Воронежа"),
        "Пермь" to ("Перми" to "Перми"),
        "Волгоград" to ("Волгограде" to "Волгограда"),
        "Тюмень" to ("Тюмени" to "Тюмени"),
        "Иркутск" to ("Иркутске" to "Иркутска"),
        "Хабаровск" to ("Хабаровске" to "Хабаровска"),
        "Ярославль" to ("Ярославле" to "Ярославля"),
        "Тула" to ("Туле" to "Тулы"),
        "Рязань" to ("Рязани" to "Рязани"),
        "Белгород" to ("Белгороде" to "Белгорода"),
        "Сургут" to ("Сургуте" to "Сургута"),
        "Мурманск" to ("Мурманске" to "Мурманска"),
        "Владимир" to ("Владимире" to "Владимира"),
        "Тверь" to ("Твери" to "Твери"),
        "Калуга" to ("Калуге" to "Калуги"),
        "Смоленск" to ("Смоленске" to "Смоленска"),
        "Псков" to ("Пскове" to "Пскова"),
        "Астрахань" to ("Астрахани" to "Астрахани"),
        "Крым" to ("Крыму" to "Крыма"),
        "Севастополь" to ("Севастополе" to "Севастополя"),
        "Зеленоград" to ("Зеленограде" to "Зеленограда"),
        "Люберцы" to ("Люберцах" to "Люберец"),
        "Серпухов" to ("Серпухове" to "Серпухова"),
        "Ростовка" to ("Ростовке" to "Ростовки"),
        "Минеральные Воды" to ("Минеральных Водах" to "Минеральных Вод"),
        "Красногорск" to ("Красногорске" to "Красногорска"),
    )

const val SEO_PAGE_DESCRIPTION_SUFFIX =
    ". Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!"

const val SEO_PAGE_TITLE_SUFFIX = " через сервис DriveBit"

private val SEO_OPTIMIZED_CITY_SLUGS = setOf("moskva", "krasnogorsk")

private val SEO_OPTIMIZED_MOSKVA_FILTER_SLUGS =
    setOf(
        "puteshestviya",
        "za-gorod",
        "poblizosti",
        "arenda-vnedorozhnika-bez-voditelya",
    )

fun isSeoOptimizedPage(
    citySlug: String,
    filterSlug: String?,
): Boolean {
    val normalizedCity = citySlug.trim().lowercase()
    val normalizedFilter = filterSlug?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
    return when {
        normalizedCity == "moskva" && normalizedFilter == null -> true
        normalizedCity == "moskva" && normalizedFilter in SEO_OPTIMIZED_MOSKVA_FILTER_SLUGS -> true
        normalizedCity == "krasnogorsk" && normalizedFilter == null -> true
        else -> false
    }
}

fun seoPageHeadline(
    cityName: String,
    filterTitle: String,
): String {
    val trimmedCity = cityName.trim()
    if (trimmedCity.isEmpty()) return "Аренда авто у частных владельцев"
    val prep = cityInPrepositional(trimmedCity)
    val gen = cityInGenitive(trimmedCity)
    return when (filterTitle) {
        "Путешествия" -> "Аренда авто для путешествий по России из $gen"
        "За город" -> "Аренда авто для поездки в другой город из $gen"
        "Поблизости" -> "Аренда авто на карте в $prep"
        "Внедорожник" -> "Аренда внедорожника без водителя в $prep"
        else -> "Аренда авто у частных владельцев в $prep"
    }
}

fun seoPageTitle(
    cityName: String,
    filterTitle: String,
): String {
    val headline = seoPageHeadline(cityName, filterTitle)
    return if (filterTitle == "Поблизости") {
        val prep = cityInPrepositional(cityName.trim())
        "Аренда авто на карте в $prep - аренда автомобиля поблизости$SEO_PAGE_TITLE_SUFFIX"
    } else {
        "$headline$SEO_PAGE_TITLE_SUFFIX"
    }
}

fun seoPageDescription(
    cityName: String,
    filterTitle: String,
): String = "${seoPageTitle(cityName, filterTitle)}$SEO_PAGE_DESCRIPTION_SUFFIX"

fun pageHeadline(
    citySlug: String,
    filterSlug: String?,
    cityName: String,
    filterTitle: String,
): String =
    if (isSeoOptimizedPage(citySlug, filterSlug)) {
        seoPageHeadline(cityName, filterTitle)
    } else {
        heroBannerHeadline(cityName, filterTitle)
    }

fun pageTitle(
    citySlug: String,
    filterSlug: String?,
    cityName: String,
    filterTitle: String,
): String =
    if (isSeoOptimizedPage(citySlug, filterSlug)) {
        seoPageTitle(cityName, filterTitle)
    } else {
        heroBannerPageTitle(cityName, filterTitle)
    }

fun pageDescription(
    citySlug: String,
    filterSlug: String?,
    cityName: String,
    filterTitle: String,
): String =
    if (isSeoOptimizedPage(citySlug, filterSlug)) {
        seoPageDescription(cityName, filterTitle)
    } else {
        cityPageDescription(cityName, filterTitle)
    }

fun heroBannerHeadline(
    cityName: String,
    filterTitle: String,
): String {
    val base = "Арендуй авто у частных владельцев"
    val trimmedCity = cityName.trim()
    if (trimmedCity.isEmpty()) return base
    val cityPhrase =
        if (filterTitle in FILTERS_WITH_GENITIVE_FROM_CITY) {
            "из ${cityInGenitive(trimmedCity)}"
        } else {
            "в ${cityInPrepositional(trimmedCity)}"
        }
    return "$base $cityPhrase"
}

fun heroBannerPageTitle(
    cityName: String,
    filterTitle: String,
): String = "${heroBannerHeadline(cityName, filterTitle)} - DriveBit"

private const val CITY_FILTER_RENT_SUFFIX =
    "Аренда у собственников, прозрачные условия и поддержка 24/7."

private const val DEFAULT_CITY_PAGE_DESCRIPTION =
    "Аренда автомобилей от собственников. Дешевле проката на 40%. Полная страховка. Поддержка 24/7."

fun cityPageDescription(
    cityName: String,
    filterTitle: String,
): String {
    val trimmedCity = cityName.trim()
    if (trimmedCity.isEmpty()) return DEFAULT_CITY_PAGE_DESCRIPTION
    val prep = cityInPrepositional(trimmedCity)
    val gen = cityInGenitive(trimmedCity)
    return when (filterTitle) {
        "К родным" ->
            "Подберите автомобиль для поездки к родным в $prep. $CITY_FILTER_RENT_SUFFIX"
        "Командировки" ->
            "Подберите автомобиль для командировки в $prep. $CITY_FILTER_RENT_SUFFIX"
        "Каникулы" ->
            "Подберите автомобиль на каникулы в $prep. $CITY_FILTER_RENT_SUFFIX"
        "Внедорожник" ->
            "Подберите внедорожник в аренду в $prep. $CITY_FILTER_RENT_SUFFIX"
        "Переезд" ->
            "Подберите автомобиль для переезда в $prep. $CITY_FILTER_RENT_SUFFIX"
        "Поблизости" ->
            "Найдите автомобили поблизости в $prep. Быстрая аренда у собственников, прозрачные условия и поддержка 24/7."
        "Путешествия" ->
            "Подберите автомобиль для путешествий из $gen. $CITY_FILTER_RENT_SUFFIX"
        "За город" ->
            "Подберите автомобиль для поездки за город из $gen. $CITY_FILTER_RENT_SUFFIX"
        else ->
            "Аренда авто в $prep у собственников. Дешевле проката, полная страховка и поддержка 24/7."
    }
}

fun cityNameFromSlug(slug: String): String? {
    val normalized = slug.trim().lowercase()
    if (normalized.isEmpty()) return null
    return CITY_FORMS.keys.firstOrNull { cityNameToSlug(it) == normalized }
}

fun resolveCityNameForMeta(
    citySlug: String,
    storedCityName: String,
): String {
    val trimmedStored = storedCityName.trim()
    if (trimmedStored.isNotEmpty()) return trimmedStored
    return cityNameFromSlug(citySlug) ?: DEFAULT_CITY_NAME
}

fun cityInPrepositional(nominative: String): String = cityForms(nominative).first

fun cityInGenitive(nominative: String): String = cityForms(nominative).second

private fun cityForms(nominative: String): Pair<String, String> {
    val key = nominative.trim()
    CITY_FORMS[key]?.let { return it }
    return declineHeuristic(key)
}

private fun declineHeuristic(nominative: String): Pair<String, String> {
    val name = nominative.trim()
    if (name.isEmpty()) return "" to ""
    return when {
        name.endsWith("ия", ignoreCase = true) -> {
            val stem = name.dropLast(2)
            "${stem}ии" to "${stem}ии"
        }
        name.endsWith("а", ignoreCase = true) -> {
            val stem = name.dropLast(1)
            "${stem}е" to "${stem}ы"
        }
        name.endsWith("я", ignoreCase = true) -> {
            val stem = name.dropLast(1)
            "${stem}е" to "${stem}и"
        }
        name.endsWith("ь", ignoreCase = true) -> {
            val stem = name.dropLast(1)
            "${stem}и" to "${stem}и"
        }
        name.endsWith("й", ignoreCase = true) -> {
            val stem = name.dropLast(1)
            "${stem}е" to "${stem}я"
        }
        else -> {
            "${name}е" to "${name.dropLast(1)}а"
        }
    }
}
