package my.drivebit.web

import my.drivebit.navigation.SeoLandingBlock
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchPageHeadlineTest {
    @Test
    fun `resolveSearchPageHeadline uses landing block h2 for brand path`() {
        val block =
            SeoLandingBlock(
                ariaLabel = "Аренда BMW в Москве",
                h2 = "Аренда BMW в Москве без водителя",
                paragraphs = emptyList(),
            )
        assertEquals(
            "Аренда BMW в Москве без водителя",
            resolveSearchPageHeadline("/search/bmw", null, block = block),
        )
    }

    @Test
    fun `resolveSearchPageHeadline prefers explicit h1`() {
        val block =
            SeoLandingBlock(
                ariaLabel = "Аренда BMW в Москве",
                h2 = "Аренда BMW в Москве без водителя",
                h1 = "Аренда BMW без водителя в Москве",
                paragraphs = emptyList(),
            )
        assertEquals(
            "Аренда BMW без водителя в Москве",
            resolveSearchPageHeadline("/bmw", null, block = block),
        )
    }

    @Test
    fun `resolveSearchPageHeadline uses audi seo spec h1`() {
        val block =
            SeoLandingBlock(
                ariaLabel = "Аренда Audi в Москве",
                h2 = "Аренда Audi без водителя в Москве",
                h1 = "Аренда Audi без водителя в Москве",
                paragraphs = emptyList(),
            )
        assertEquals(
            "Аренда Audi без водителя в Москве",
            resolveSearchPageHeadline("/audi", null, block = block),
        )
    }

    @Test
    fun `resolveSearchPageHeadline uses search root block`() {
        val block =
            SeoLandingBlock(
                ariaLabel = "Поиск автомобилей DriveBit",
                h2 = "Поиск автомобилей для аренды в Москве",
                paragraphs = emptyList(),
            )
        assertEquals(
            "Поиск автомобилей для аренды в Москве",
            resolveSearchPageHeadline("/moskva/search", null, block = block),
        )
    }

    @Test
    fun `resolveSearchPageHeadline uses city name from url`() {
        assertEquals(
            "Поиск автомобилей для аренды в Калининграде",
            resolveSearchPageHeadline("/kaliningrad/search", null, cityName = "Калининград", block = null),
        )
    }

    @Test
    fun `resolveSearchPageHeadline falls back to brand name`() {
        assertEquals(
            "Аренда Toyota в Москве без водителя",
            resolveSearchPageHeadline("/moskva/search", "Toyota", block = null),
        )
    }
}
