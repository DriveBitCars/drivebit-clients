package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class CityDeclensionUtilsTest {
    @Test
    fun cityInPrepositional_knownCities() {
        assertEquals("Москве", cityInPrepositional("Москва"))
        assertEquals("Санкт-Петербурге", cityInPrepositional("Санкт-Петербург"))
        assertEquals("Казани", cityInPrepositional("Казань"))
        assertEquals("Сочи", cityInPrepositional("Сочи"))
    }

    @Test
    fun cityInGenitive_knownCities() {
        assertEquals("Москвы", cityInGenitive("Москва"))
        assertEquals("Санкт-Петербурга", cityInGenitive("Санкт-Петербург"))
        assertEquals("Казани", cityInGenitive("Казань"))
        assertEquals("Сочи", cityInGenitive("Сочи"))
    }

    @Test
    fun heroBannerHeadline_usesLocativeForDefaultFilters() {
        assertEquals(
            "Арендуй авто у частных владельцев в Москве",
            heroBannerHeadline("Москва", "Все"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев в Москве",
            heroBannerHeadline("Москва", "Поблизости"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев в Москве",
            heroBannerHeadline("Москва", "Командировки"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев в Москве",
            heroBannerHeadline("Москва", "К родным"),
        )
    }

    @Test
    fun heroBannerHeadline_usesGenitiveForTravelFilters() {
        assertEquals(
            "Арендуй авто у частных владельцев из Москвы",
            heroBannerHeadline("Москва", "Путешествия"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев из Москвы",
            heroBannerHeadline("Москва", "За город"),
        )
    }

    @Test
    fun heroBannerHeadline_withoutCity() {
        assertEquals(
            "Арендуй авто у частных владельцев",
            heroBannerHeadline("", "Все"),
        )
    }

    @Test
    fun heroBannerPageTitle_appendsDriveBitSuffix() {
        assertEquals(
            "Арендуй авто у частных владельцев из Москвы - DriveBit",
            heroBannerPageTitle("Москва", "Путешествия"),
        )
    }

    @Test
    fun cityPageDescription_defaultFilter_usesCityInPrepositional() {
        assertEquals(
            "Аренда авто в Зеленограде у собственников. Дешевле проката, полная страховка и поддержка 24/7.",
            cityPageDescription("Зеленоград", "Все"),
        )
        assertEquals(
            "Аренда авто в Москве у собственников. Дешевле проката, полная страховка и поддержка 24/7.",
            cityPageDescription("Москва", "Все"),
        )
    }

    @Test
    fun cityPageDescription_travelFilter_usesGenitive() {
        assertEquals(
            "Подберите автомобиль для путешествий из Москвы. Аренда у собственников, прозрачные условия и поддержка 24/7.",
            cityPageDescription("Москва", "Путешествия"),
        )
    }

    @Test
    fun cityNameFromSlug_resolvesKnownCities() {
        assertEquals("Москва", cityNameFromSlug("moskva"))
        assertEquals("Казань", cityNameFromSlug("kazan"))
        assertEquals("Зеленоград", cityNameFromSlug("zelenograd"))
    }

    @Test
    fun resolveCityNameForMeta_prefersSlugWhenKnown() {
        assertEquals("Москва", resolveCityNameForMeta("moskva", "Калининград"))
        assertEquals("Красногорск", resolveCityNameForMeta("krasnogorsk", "Москва"))
        assertEquals("Калининград", resolveCityNameForMeta("unknown-slug", "Калининград"))
        assertEquals("Москва", resolveCityNameForMeta("moskva", ""))
    }

    @Test
    fun heroBannerHeadline_otherCity() {
        assertEquals(
            "Арендуй авто у частных владельцев в Казани",
            heroBannerHeadline("Казань", "Поблизости"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев из Казани",
            heroBannerHeadline("Казань", "Путешествия"),
        )
    }

    @Test
    fun isSeoOptimizedPage_whitelist() {
        assertEquals(true, isSeoOptimizedPage("moskva", null))
        assertEquals(true, isSeoOptimizedPage("moskva", "puteshestviya"))
        assertEquals(true, isSeoOptimizedPage("moskva", "za-gorod"))
        assertEquals(true, isSeoOptimizedPage("moskva", "poblizosti"))
        assertEquals(true, isSeoOptimizedPage("krasnogorsk", null))
        assertEquals(false, isSeoOptimizedPage("moskva", "kanikuly"))
        assertEquals(false, isSeoOptimizedPage("kazan", null))
    }

    @Test
    fun seoPageMeta_goldenFivePages() {
        assertEquals(
            "Аренда авто у частных владельцев в Москве",
            seoPageHeadline("Москва", "Все"),
        )
        assertEquals(
            "Аренда авто у частных владельцев в Москве через сервис DriveBit",
            seoPageTitle("Москва", "Все"),
        )
        assertEquals(
            "Аренда авто для путешествий по России из Москвы через сервис DriveBit. Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!",
            seoPageDescription("Москва", "Путешествия"),
        )
        assertEquals(
            "Аренда авто на карте в Москве - аренда автомобиля поблизости через сервис DriveBit",
            seoPageTitle("Москва", "Поблизости"),
        )
        assertEquals(
            "Аренда авто у частных владельцев в Красногорске через сервис DriveBit",
            seoPageTitle("Красногорск", "Все"),
        )
    }

    @Test
    fun pageTitle_legacyUnchangedForNonWhitelist() {
        assertEquals(
            "Арендуй авто у частных владельцев в Москве - DriveBit",
            pageTitle("moskva", "kanikuly", "Москва", "Каникулы"),
        )
        assertEquals(
            "Аренда авто у частных владельцев в Москве через сервис DriveBit",
            pageTitle("moskva", null, "Москва", "Все"),
        )
    }

    @Test
    fun cityInPrepositional_krasnogorsk() {
        assertEquals("Красногорске", cityInPrepositional("Красногорск"))
        assertEquals("Красногорска", cityInGenitive("Красногорск"))
    }
}
