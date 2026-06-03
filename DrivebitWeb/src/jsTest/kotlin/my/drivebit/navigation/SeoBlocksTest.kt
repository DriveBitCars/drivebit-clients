package my.drivebit.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SeoBlocksTest {
    @Test
    fun parseBlocksJson_moskvaFilters_areDistinct() {
        val blocks = SeoLandingBlocks.parseBlocksJson(FIXTURE_JSON)
        val moskva = blocks["/moskva"]
        val kanikuly = blocks["/moskva/kanikuly"]
        val komandirovki = blocks["/moskva/komandirovki"]

        assertNotNull(moskva)
        assertNotNull(kanikuly)
        assertNotNull(komandirovki)
        assertNotEquals(moskva.h2, kanikuly.h2)
        assertNotEquals(moskva.h2, komandirovki.h2)
        assertNotEquals(kanikuly.paragraphs.first(), komandirovki.paragraphs.first())
    }

    @Test
    fun normalizePath_trimsTrailingSlash() {
        assertEquals("/moskva/kanikuly", SeoLandingBlocks.normalizePath("/moskva/kanikuly/"))
    }

    @Test
    fun blockForPath_usesNormalizedKey() {
        val blocks = SeoLandingBlocks.parseBlocksJson(FIXTURE_JSON)
        val kanikuly = blocks[SeoLandingBlocks.normalizePath("/moskva/kanikuly/")]
        assertNotNull(kanikuly)
        assertEquals("Аренда авто для поездки в Крым и на каникулы", kanikuly.h2)
    }

    @Test
    fun renderSectionInnerHtml_rendersSectionsTableAndBullets() {
        val block =
            SeoLandingBlocks.parseBlocksJson(PUTESHESTVIYA_FIXTURE_JSON)["/moskva/puteshestviya"]!!
        val html = SeoLandingBlocks.renderSectionInnerHtml(block)
        assertTrue(html.contains("<h2>Почему путешествие"))
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("<th>Класс авто</th>"))
        assertTrue(html.contains("<td>Минивэн</td>"))
    }

    @Test
    fun parseBlocksJson_krasnogorskBlockExists() {
        val blocks = SeoLandingBlocks.parseBlocksJson(KRASNOGORSK_FIXTURE_JSON)
        val block = blocks["/krasnogorsk"]
        assertNotNull(block)
        assertEquals("Аренда авто у частных владельцев в Красногорске", block.h2)
    }

    private companion object {
        val FIXTURE_JSON =
            """
            {
              "/moskva": {
                "ariaLabel": "Аренда внедорожника в Москве",
                "h2": "Аренда внедорожника в Москве без водителя",
                "paragraphs": ["p1", "p2"]
              },
              "/moskva/kanikuly": {
                "ariaLabel": "Аренда авто на каникулы",
                "h2": "Аренда авто для поездки в Крым и на каникулы",
                "paragraphs": ["k1", "k2"]
              },
              "/moskva/komandirovki": {
                "ariaLabel": "Аренда авто для командировок",
                "h2": "Аренда авто для командировки и поездки в другой город",
                "paragraphs": ["c1", "c2"]
              }
            }
            """.trimIndent()

        val PUTESHESTVIYA_FIXTURE_JSON =
            """
            {
              "/moskva/puteshestviya": {
                "ariaLabel": "Аренда авто для путешествий по России",
                "h2": "Аренда авто для путешествий по России из Москвы",
                "paragraphs": [],
                "sections": [
                  {
                    "heading": "Аренда авто для путешествий по России из Москвы",
                    "paragraphs": ["intro"]
                  },
                  {
                    "heading": "Почему путешествие на арендованном авто — удобный выбор",
                    "bullets": ["one", "two"]
                  },
                  {
                    "heading": "Какой автомобиль выбрать для поездки",
                    "table": {
                      "headers": ["Класс авто", "Примеры моделей"],
                      "rows": [["Минивэн", "Hyundai Starex"]]
                    }
                  }
                ]
              }
            }
            """.trimIndent()

        val KRASNOGORSK_FIXTURE_JSON =
            """
            {
              "/krasnogorsk": {
                "ariaLabel": "Аренда авто в Красногорске",
                "h2": "Аренда авто у частных владельцев в Красногорске",
                "paragraphs": ["p1", "p2"]
              }
            }
            """.trimIndent()
    }
}
