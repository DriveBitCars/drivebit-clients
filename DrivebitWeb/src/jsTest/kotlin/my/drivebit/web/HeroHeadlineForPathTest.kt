package my.drivebit.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HeroHeadlineForPathTest {
    @Test
    fun heroHeadlineForCityPath_moskvaRoot() {
        assertEquals(
            "Аренда авто у частных владельцев в Москве",
            heroHeadlineForCityPath("/moskva"),
        )
    }

    @Test
    fun heroHeadlineForCityPath_changesWhenFilterSegmentChanges() {
        val rootHeadline = heroHeadlineForCityPath("/moskva")
        val krymHeadline = heroHeadlineForCityPath("/moskva/arenda-avto-v-krym")

        assertNotNull(rootHeadline)
        assertNotNull(krymHeadline)
        assertNotEquals(rootHeadline, krymHeadline)
        assertEquals("Аренда авто для поездки в Крым из Москвы", krymHeadline)
    }

    @Test
    fun heroHeadlineForCityPath_ignoresQueryString() {
        assertEquals(
            heroHeadlineForCityPath("/moskva/arenda-avto-v-krym"),
            heroHeadlineForCityPath("/moskva/arenda-avto-v-krym?startDate=2026-07-01"),
        )
    }

    @Test
    fun heroHeadlineForCityPath_returnsNullForNonCityPath() {
        assertNull(heroHeadlineForCityPath("/search"))
        assertNull(heroHeadlineForCityPath("/profile"))
    }

    @Test
    fun activeFilterTitleForCityPath_readsOnlyFromPath() {
        assertEquals("Все", activeFilterTitleForCityPath("/moskva"))
        assertEquals("В Крым", activeFilterTitleForCityPath("/moskva/arenda-avto-v-krym"))
        assertEquals("Поблизости", activeFilterTitleForCityPath("/moskva/poblizosti"))
        assertEquals("Комфорт", activeFilterTitleForCityPath("/moskva/arenda-avto-komfort-klassa-bez-voditelya"))
    }

    @Test
    fun heroHeadlineForCityPath_komfort() {
        assertEquals(
            "Аренда авто комфорт-класса без водителя в Москве",
            heroHeadlineForCityPath("/moskva/arenda-avto-komfort-klassa-bez-voditelya"),
        )
    }
}
