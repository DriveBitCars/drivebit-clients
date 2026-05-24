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
    )

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
