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
            "Арендуй авто у частных владельцев из Москвы",
            heroBannerHeadline("Москва", "Абхазия"),
        )
        assertEquals(
            "Арендуй авто у частных владельцев из Москвы",
            heroBannerHeadline("Москва", "Беларусь"),
        )
    }

    @Test
    fun heroBannerHeadline_usesGenitiveForTravelFilters() {
        assertEquals(
            "Арендуй авто у частных владельцев из Москвы",
            heroBannerHeadline("Москва", "В Крым"),
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
            heroBannerPageTitle("Москва", "В Крым"),
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
            "Подберите автомобиль для поездки в Крым из Москвы. Аренда у собственников, прозрачные условия и поддержка 24/7.",
            cityPageDescription("Москва", "В Крым"),
        )
    }

    @Test
    fun cityNameFromSlug_resolvesKnownCities() {
        assertEquals("Москва", cityNameFromSlug("moskva"))
        assertEquals("Казань", cityNameFromSlug("kazan"))
        assertEquals("Зеленоград", cityNameFromSlug("zelenograd"))
    }

    @Test
    fun resolveCityNameForMeta_prefersStoredName() {
        assertEquals("Калининград", resolveCityNameForMeta("moskva", "Калининград"))
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
            heroBannerHeadline("Казань", "В Крым"),
        )
    }

    @Test
    fun isSeoOptimizedPage_whitelist() {
        assertEquals(true, isSeoOptimizedPage("moskva", null))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-v-krym"))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-v-belarus"))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-v-abkhaziyu"))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-premium-klassa-bez-voditelya"))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-ekonom-klassa-bez-voditelya"))
        assertEquals(true, isSeoOptimizedPage("moskva", "arenda-avto-komfort-klassa-bez-voditelya"))
        assertEquals(true, isSeoOptimizedPage("moskva", "poblizosti"))
        assertEquals(true, isSeoOptimizedPage("krasnogorsk", null))
        assertEquals(false, isSeoOptimizedPage("moskva", "unknown-filter"))
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
            "Аренда авто для поездки в Крым из Москвы через сервис DriveBit. Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!",
            seoPageDescription("Москва", "В Крым"),
        )
        assertEquals(
            "Аренда авто на карте в Москве - аренда автомобиля поблизости через сервис DriveBit",
            seoPageTitle("Москва", "Поблизости"),
        )
        assertEquals(
            "Аренда авто у частных владельцев в Красногорске через сервис DriveBit",
            seoPageTitle("Красногорск", "Все"),
        )
        assertEquals(
            "Аренда авто комфорт-класса без водителя в Москве",
            seoPageHeadline("Москва", "Комфорт"),
        )
        assertEquals(
            "Аренда автомобиля комфорт-класса в Москве через сервис DriveBit",
            seoPageTitle("Москва", "Комфорт"),
        )
        assertEquals(
            "Аренда автомобиля комфорт-класса в Москве через сервис DriveBit. Безопасно и быстро. Чистые и ухоженные автомобили дешевле каршеринга!",
            seoPageDescription("Москва", "Комфорт"),
        )
    }

    @Test
    fun pageTitle_legacyUnchangedForNonWhitelist() {
        assertEquals(
            "Аренда авто для поездки в Абхазию из Москвы через сервис DriveBit",
            pageTitle("moskva", "arenda-avto-v-abkhaziyu", "Москва", "Абхазия"),
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
