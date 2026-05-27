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
            heroBannerHeadline("Казань", "Путешествия"),
        )
    }
}
